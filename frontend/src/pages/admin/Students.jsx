import { useState, useEffect, useMemo } from 'react';
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, IconButton, CircularProgress, Alert, Chip, Grid, MenuItem,
  InputAdornment, Collapse, Avatar,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import BusinessIcon from '@mui/icons-material/Business';
import SchoolIcon from '@mui/icons-material/School';
import { getStudents, createStudent, updateStudent, deleteStudent } from '../../api/students';
import { getDepartments } from '../../api/departments';

const LEVELS = ['HND 1', 'HND2', 'B-TECH', 'M-TECH 1', 'M-TECH 2'];
const LEVEL_COLORS = { 'HND 1': '#1565c0', HND2: '#2e7d32', 'B-TECH': '#e65100', 'M-TECH 1': '#c62828', 'M-TECH 2': '#6a1b9a' };
const LEVEL_BG = { 'HND 1': '#e3f2fd', HND2: '#e8f5e9', 'B-TECH': '#fff3e0', 'M-TECH 1': '#ffebee', 'M-TECH 2': '#f3e5f5' };

const emptyStudent = { firstName: '', lastName: '', email: '', phone: '', dateOfBirth: '', enrollmentYear: new Date().getFullYear(), currentLevel: 'HND 1', programName: '', departmentId: '', password: '' };

function groupByDeptAndLevel(students, deptMap) {
  const groups = {};
  for (const s of students) {
    const deptName = deptMap[s.departmentId] || 'Unassigned';
    if (!groups[deptName]) groups[deptName] = {};
    const level = s.currentLevel || 'Unknown';
    if (!groups[deptName][level]) groups[deptName][level] = [];
    groups[deptName][level].push(s);
  }
  return groups;
}

export default function AdminStudents() {
  const [students, setStudents] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyStudent);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchName, setSearchName] = useState('');
  const [searchDept, setSearchDept] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [expandedDepts, setExpandedDepts] = useState({});
  const [expandedLevels, setExpandedLevels] = useState({});

  const deptMap = useMemo(() => {
    const m = {};
    for (const d of departments) m[d.id] = d.name;
    return m;
  }, [departments]);

  const fetchData = async () => {
    try {
      const [sRes, dRes] = await Promise.all([getStudents(), getDepartments()]);
      setStudents(sRes.data);
      setDepartments(dRes.data);
      const initDepts = {};
      const initLevels = {};
      for (const d of dRes.data) { initDepts[d.name] = true; }
      setExpandedDepts(initDepts);
      setExpandedLevels(initLevels);
    } catch { setError('Failed to load data'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyStudent); setDialog(true); };
  const openEdit = (s) => { setEditing(s); setForm({ ...s, password: '' }); setDialog(true); };

  const handleSave = async () => {
    setError('');
    try {
      if (editing) {
        await updateStudent(editing.id, form);
      } else {
        await createStudent(form);
      }
      setDialog(false);
      fetchData();
    } catch (err) { setError(err.response?.data?.message || 'Operation failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this student?')) return;
    try { await deleteStudent(id); fetchData(); }
    catch { setError('Delete failed'); }
  };

  const toggleDept = (name) => setExpandedDepts(prev => ({ ...prev, [name]: !prev[name] }));
  const toggleLevel = (key) => setExpandedLevels(prev => ({ ...prev, [key]: !prev[key] }));

  const filteredStudents = useMemo(() => {
    return students.filter((s) => {
      const matchesName = !searchName || s.fullName?.toLowerCase().includes(searchName.toLowerCase()) || s.email?.toLowerCase().includes(searchName.toLowerCase());
      const matchesDept = !searchDept || String(s.departmentId) === searchDept;
      return matchesName && matchesDept;
    });
  }, [students, searchName, searchDept]);

  const grouped = useMemo(() => groupByDeptAndLevel(filteredStudents, deptMap), [filteredStudents, deptMap]);
  const deptNames = Object.keys(grouped).sort();

  if (loading) return <Box display="flex" justifyContent="center" py={4}><CircularProgress /></Box>;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4" fontWeight="bold">Students</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Student</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Box display="flex" gap={2} mb={3}>
        <TextField
          placeholder="Search by name or email..."
          size="small"
          value={searchName}
          onChange={(e) => setSearchName(e.target.value)}
          sx={{ minWidth: 250 }}
        />
        <TextField
          select
          size="small"
          label="Department"
          value={searchDept}
          onChange={(e) => setSearchDept(e.target.value)}
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All Departments</MenuItem>
          {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
        </TextField>
      </Box>

      {deptNames.length === 0 && (
        <Alert severity="info">No students found matching your criteria.</Alert>
      )}

      {deptNames.map((deptName) => {
        const levels = grouped[deptName];
        const levelKeys = Object.keys(levels).sort((a, b) => {
          const levelOrder = { 'HND 1': 1, HND2: 2, 'B-TECH': 3, 'M-TECH 1': 4, 'M-TECH 2': 5 };
          return (levelOrder[a] || 0) - (levelOrder[b] || 0);
        });
        const totalInDept = levelKeys.reduce((sum, lv) => sum + levels[lv].length, 0);
        const deptOpen = expandedDepts[deptName] !== false;

        return (
          <Box key={deptName} sx={{ mb: 3 }}>
            <Paper
              elevation={0}
              sx={{
                p: 2,
                bgcolor: '#e3f2fd',
                borderLeft: '4px solid #1565c0',
                cursor: 'pointer',
                '&:hover': { bgcolor: '#bbdefb' },
              }}
              onClick={() => toggleDept(deptName)}
            >
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box display="flex" alignItems="center" gap={1.5}>
                  <Avatar sx={{ bgcolor: '#1565c0', width: 36, height: 36 }}>
                    <BusinessIcon sx={{ fontSize: 20 }} />
                  </Avatar>
                  <Box>
                    <Typography variant="h6" fontWeight="bold">{deptName}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {totalInDept} student{totalInDept !== 1 ? 's' : ''} across {levelKeys.length} level{levelKeys.length !== 1 ? 's' : ''}
                    </Typography>
                  </Box>
                </Box>
                {deptOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </Box>
            </Paper>

            <Collapse in={deptOpen} timeout="auto">
              <Box sx={{ pl: 2 }}>
                {levelKeys.map((level) => {
                  const levelStudents = levels[level];
                  const levelKey = `${deptName}-${level}`;
                  const levelOpen = expandedLevels[levelKey] !== false;

                  return (
                    <Box key={levelKey} sx={{ mt: 2 }}>
                      <Paper
                        elevation={0}
                        sx={{
                          py: 1.5,
                          px: 2,
                          bgcolor: LEVEL_BG[level] || '#f5f5f5',
                          borderLeft: `4px solid ${LEVEL_COLORS[level] || '#9e9e9e'}`,
                          cursor: 'pointer',
                          '&:hover': { opacity: 0.9 },
                        }}
                        onClick={() => toggleLevel(levelKey)}
                      >
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                          <Box display="flex" alignItems="center" gap={1}>
                            <SchoolIcon sx={{ color: LEVEL_COLORS[level] || '#9e9e9e', fontSize: 22 }} />
                            <Typography variant="subtitle1" fontWeight="bold">
                              {level}
                            </Typography>
                            <Chip
                              label={`${levelStudents.length} student${levelStudents.length !== 1 ? 's' : ''}`}
                              size="small"
                              sx={{ bgcolor: LEVEL_COLORS[level] || '#9e9e9e', color: '#fff' }}
                            />
                          </Box>
                          {levelOpen ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                        </Box>
                      </Paper>

                      <Collapse in={levelOpen} timeout="auto">
                        <TableContainer sx={{ mt: 1 }}>
                          <Table size="small">
                            <TableHead>
                              <TableRow sx={{ bgcolor: '#fafafa' }}>
                                <TableCell sx={{ fontWeight: 600 }}>Student ID</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Name</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Email</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>CGPA</TableCell>
                                <TableCell align="right" sx={{ fontWeight: 600 }}>Actions</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {levelStudents.map((s) => (
                                <TableRow key={s.id} hover>
                                  <TableCell>{s.studentId}</TableCell>
                                  <TableCell>{s.fullName}</TableCell>
                                  <TableCell>{s.email}</TableCell>
                                  <TableCell>
                                    <Chip
                                      label={s.status}
                                      size="small"
                                      color={s.status === 'ACTIVE' ? 'success' : 'warning'}
                                    />
                                  </TableCell>
                                  <TableCell>{s.cgpa?.toFixed(2) || 'N/A'}</TableCell>
                                  <TableCell align="right">
                                    <IconButton size="small" onClick={() => openEdit(s)}>
                                      <EditIcon fontSize="small" />
                                    </IconButton>
                                    <IconButton size="small" onClick={() => handleDelete(s.id)} color="error">
                                      <DeleteIcon fontSize="small" />
                                    </IconButton>
                                  </TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </TableContainer>
                      </Collapse>
                    </Box>
                  );
                })}
              </Box>
            </Collapse>
          </Box>
        );
      })}

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="lg" fullWidth>
        <DialogTitle>{editing ? 'Edit Student' : 'Add Student'}</DialogTitle>
        <DialogContent sx={{ maxHeight: '80vh', pt: '16px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField label="First Name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Last Name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Date of Birth" type="date" value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} InputLabelProps={{ shrink: true }} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Enrollment Year" type="number" value={form.enrollmentYear} onChange={(e) => setForm({ ...form, enrollmentYear: Number(e.target.value) })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField select label="Current Level" value={form.currentLevel} onChange={(e) => setForm({ ...form, currentLevel: e.target.value })} fullWidth size="small">
                {LEVELS.map((lv) => <MenuItem key={lv} value={lv}>{lv}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField label="Program Name" value={form.programName} onChange={(e) => setForm({ ...form, programName: e.target.value })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField select label="Department" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })} fullWidth size="small">
                {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
              </TextField>
            </Grid>
            {!editing && (
              <Grid item xs={12}>
                <TextField
                  label="Password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required fullWidth size="small"
                  type={showPassword ? 'text' : 'password'}
                  slotProps={{
                    input: {
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small">
                            {showPassword ? <VisibilityOff /> : <Visibility />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    },
                  }}
                />
              </Grid>
            )}
            {editing && (
              <Grid item xs={12}>
                <TextField select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} fullWidth size="small">
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="SUSPENDED">Suspended</MenuItem>
                  <MenuItem value="GRADUATED">Graduated</MenuItem>
                  <MenuItem value="WITHDRAWN">Withdrawn</MenuItem>
                </TextField>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">{editing ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
