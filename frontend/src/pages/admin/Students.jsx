import { useState, useEffect } from 'react';
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, IconButton, CircularProgress, Alert, Chip, MenuItem,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { getStudents, createStudent, updateStudent, deleteStudent } from '../../api/students';
import { getDepartments } from '../../api/departments';

const emptyStudent = { firstName: '', lastName: '', email: '', phone: '', dateOfBirth: '', enrollmentYear: new Date().getFullYear(), currentLevel: 100, programName: '', departmentId: '', password: '' };

export default function AdminStudents() {
  const [students, setStudents] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyStudent);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = async () => {
    try {
      const [sRes, dRes] = await Promise.all([getStudents(), getDepartments()]);
      setStudents(sRes.data);
      setDepartments(dRes.data);
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

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4" fontWeight="bold">Students</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Student</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Student ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>CGPA</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {students.map((s) => (
              <TableRow key={s.id}>
                <TableCell>{s.studentId}</TableCell>
                <TableCell>{s.fullName}</TableCell>
                <TableCell>{s.email}</TableCell>
                <TableCell>{s.currentLevel}</TableCell>
                <TableCell><Chip label={s.status} size="small" color={s.status === 'ACTIVE' ? 'success' : 'warning'} /></TableCell>
                <TableCell>{s.cgpa?.toFixed(2) || 'N/A'}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => openEdit(s)}><EditIcon /></IconButton>
                  <IconButton onClick={() => handleDelete(s.id)} color="error"><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Student' : 'Add Student'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="First Name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
            <TextField label="Last Name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
            <TextField label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            <TextField label="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            <TextField label="Date of Birth" type="date" value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} InputLabelProps={{ shrink: true }} />
            <TextField label="Enrollment Year" type="number" value={form.enrollmentYear} onChange={(e) => setForm({ ...form, enrollmentYear: Number(e.target.value) })} />
            <TextField label="Current Level" type="number" value={form.currentLevel} onChange={(e) => setForm({ ...form, currentLevel: Number(e.target.value) })} />
            <TextField label="Program Name" value={form.programName} onChange={(e) => setForm({ ...form, programName: e.target.value })} />
            <TextField select label="Department" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
              {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
            {!editing && <TextField label="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />}
            {editing && <TextField select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="SUSPENDED">Suspended</MenuItem>
              <MenuItem value="GRADUATED">Graduated</MenuItem>
              <MenuItem value="WITHDRAWN">Withdrawn</MenuItem>
            </TextField>}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">{editing ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
