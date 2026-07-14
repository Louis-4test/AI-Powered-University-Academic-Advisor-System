import { useState, useEffect } from 'react';
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, IconButton, CircularProgress, Alert, MenuItem, Grid,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { getCourses, createCourse, updateCourse, deleteCourse } from '../../api/courses';
import { getDepartments } from '../../api/departments';
import { getLecturers } from '../../api/lecturers';

const emptyCourse = { courseCode: '', title: '', description: '', creditHours: 3, level: 100, semester: 'FIRST', departmentId: '', lecturerId: '' };

export default function AdminCourses() {
  const [courses, setCourses] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [lecturers, setLecturers] = useState([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyCourse);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = async () => {
    try {
      const [cRes, dRes, lRes] = await Promise.all([getCourses(), getDepartments(), getLecturers()]);
      setCourses(cRes.data);
      setDepartments(dRes.data);
      setLecturers(lRes.data);
    } catch { setError('Failed to load'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyCourse); setDialog(true); };
  const openEdit = (c) => { setEditing(c); setForm({ ...c, departmentId: c.departmentId || '', lecturerId: c.lecturerId || '' }); setDialog(true); };

  const handleSave = async () => {
    setError('');
    try {
      if (editing) await updateCourse(editing.id, form);
      else await createCourse(form);
      setDialog(false);
      fetchData();
    } catch (err) { setError(err.response?.data?.message || 'Operation failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this course?')) return;
    try { await deleteCourse(id); fetchData(); }
    catch { setError('Delete failed'); }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4" fontWeight="bold">Courses</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Course</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>
              <TableCell>Title</TableCell>
              <TableCell>Credits</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Semester</TableCell>
              <TableCell>Department</TableCell>
              <TableCell>Lecturer</TableCell>
              <TableCell>Enrollments</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {courses.map((c) => (
              <TableRow key={c.id}>
                <TableCell>{c.courseCode}</TableCell>
                <TableCell>{c.title}</TableCell>
                <TableCell>{c.creditHours}</TableCell>
                <TableCell>{c.level}</TableCell>
                <TableCell>{c.semester}</TableCell>
                <TableCell>{c.departmentName}</TableCell>
                <TableCell>{c.lecturerName || '-'}</TableCell>
                <TableCell>{c.enrollmentCount}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => openEdit(c)}><EditIcon /></IconButton>
                  <IconButton onClick={() => handleDelete(c.id)} color="error"><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="lg" fullWidth>
        <DialogTitle>{editing ? 'Edit Course' : 'Add Course'}</DialogTitle>
        <DialogContent sx={{ maxHeight: '80vh', pt: '16px !important' }}>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField label="Course Code" value={form.courseCode} onChange={(e) => setForm({ ...form, courseCode: e.target.value })} required fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Description" multiline rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Credit Hours" type="number" value={form.creditHours} onChange={(e) => setForm({ ...form, creditHours: Number(e.target.value) })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Level" type="number" value={form.level} onChange={(e) => setForm({ ...form, level: Number(e.target.value) })} fullWidth size="small" />
            </Grid>
            <Grid item xs={12}>
              <TextField select label="Semester" value={form.semester} onChange={(e) => setForm({ ...form, semester: e.target.value })} fullWidth size="small">
                <MenuItem value="FIRST">First</MenuItem>
                <MenuItem value="SECOND">Second</MenuItem>
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField select label="Department" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })} fullWidth size="small">
                {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
              </TextField>
            </Grid>
            <Grid item xs={12}>
              <TextField select label="Lecturer" value={form.lecturerId} onChange={(e) => setForm({ ...form, lecturerId: e.target.value })} fullWidth size="small">
                <MenuItem value="">None</MenuItem>
                {lecturers.map((l) => <MenuItem key={l.id} value={l.id}>{l.fullName}</MenuItem>)}
              </TextField>
            </Grid>
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
