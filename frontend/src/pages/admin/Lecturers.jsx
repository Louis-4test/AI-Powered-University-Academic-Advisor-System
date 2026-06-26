import { useState, useEffect } from 'react';
import {
  Box, Typography, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, IconButton, CircularProgress, Alert, MenuItem,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import { getLecturers, createLecturer, updateLecturer, deleteLecturer } from '../../api/lecturers';
import { getDepartments } from '../../api/departments';

const emptyLecturer = { firstName: '', lastName: '', email: '', phone: '', qualification: '', specialization: '', departmentId: '', password: '' };

export default function AdminLecturers() {
  const [lecturers, setLecturers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [dialog, setDialog] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyLecturer);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchData = async () => {
    try {
      const [lRes, dRes] = await Promise.all([getLecturers(), getDepartments()]);
      setLecturers(lRes.data);
      setDepartments(dRes.data);
    } catch { setError('Failed to load'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyLecturer); setDialog(true); };
  const openEdit = (l) => { setEditing(l); setForm({ ...l, password: '' }); setDialog(true); };

  const handleSave = async () => {
    setError('');
    try {
      if (editing) await updateLecturer(editing.id, form);
      else await createLecturer(form);
      setDialog(false);
      fetchData();
    } catch (err) { setError(err.response?.data?.message || 'Operation failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this lecturer?')) return;
    try { await deleteLecturer(id); fetchData(); }
    catch { setError('Delete failed'); }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4" fontWeight="bold">Lecturers</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>Add Lecturer</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Lecturer ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Qualification</TableCell>
              <TableCell>Specialization</TableCell>
              <TableCell>Department</TableCell>
              <TableCell>Courses</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {lecturers.map((l) => (
              <TableRow key={l.id}>
                <TableCell>{l.lecturerId}</TableCell>
                <TableCell>{l.fullName}</TableCell>
                <TableCell>{l.email}</TableCell>
                <TableCell>{l.qualification || '-'}</TableCell>
                <TableCell>{l.specialization || '-'}</TableCell>
                <TableCell>{l.departmentName}</TableCell>
                <TableCell>{l.courseCount}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => openEdit(l)}><EditIcon /></IconButton>
                  <IconButton onClick={() => handleDelete(l.id)} color="error"><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Lecturer' : 'Add Lecturer'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField label="First Name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
            <TextField label="Last Name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
            <TextField label="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            <TextField label="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            <TextField label="Qualification" value={form.qualification} onChange={(e) => setForm({ ...form, qualification: e.target.value })} />
            <TextField label="Specialization" value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} />
            <TextField select label="Department" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
              {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
            {!editing && <TextField label="Password" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />}
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
