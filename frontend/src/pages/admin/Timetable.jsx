import { useState, useEffect } from 'react';
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, IconButton, CircularProgress, Alert, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, MenuItem, Chip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import { getTimetable, createTimetableEntry, updateTimetableEntry, deleteTimetableEntry, generateTimetable } from '../../api/timetable';
import { getCourses } from '../../api/courses';
import { getLecturers } from '../../api/lecturers';
import { getDepartments } from '../../api/departments';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
const SEMESTERS = ['FIRST', 'SECOND'];

const emptyForm = {
  courseId: '', lecturerId: '', dayOfWeek: 'MONDAY',
  startTime: '08:00', endTime: '09:30',
  room: '', location: '', departmentId: '', academicYear: '2025/2026', semester: 'FIRST',
};

export default function AdminTimetable() {
  const [entries, setEntries] = useState([]);
  const [courses, setCourses] = useState([]);
  const [lecturers, setLecturers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialog, setDialog] = useState(false);
  const [genDialog, setGenDialog] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [genForm, setGenForm] = useState({ departmentId: '', academicYear: '2025/2026', semester: 'FIRST' });
  const [genLoading, setGenLoading] = useState(false);

  const fetchData = async () => {
    try {
      const [eRes, cRes, lRes, dRes] = await Promise.all([
        getTimetable(), getCourses(), getLecturers(), getDepartments(),
      ]);
      setEntries(eRes.data);
      setCourses(cRes.data);
      setLecturers(lRes.data);
      setDepartments(dRes.data);
    } catch { setError('Failed to load'); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => { setEditing(null); setForm(emptyForm); setDialog(true); };
  const openEdit = (e) => {
    setEditing(e);
    setForm({
      courseId: e.courseId || '',
      lecturerId: e.lecturerId || '',
      dayOfWeek: e.dayOfWeek,
      startTime: e.startTime,
      endTime: e.endTime,
      room: e.room || '',
      location: e.location || '',
      departmentId: e.departmentId || '',
      academicYear: e.academicYear || '2025/2026',
      semester: e.semester || 'FIRST',
    });
    setDialog(true);
  };

  const handleSave = async () => {
    setError('');
    try {
      if (editing) await updateTimetableEntry(editing.id, form);
      else await createTimetableEntry(form);
      setDialog(false);
      fetchData();
    } catch (err) { setError(err.response?.data?.message || 'Operation failed'); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this timetable entry?')) return;
    try { await deleteTimetableEntry(id); fetchData(); }
    catch { setError('Delete failed'); }
  };

  const handleGenerate = async () => {
    setGenLoading(true);
    setError('');
    try {
      await generateTimetable(genForm);
      setGenDialog(false);
      fetchData();
    } catch (err) { setError(err.response?.data?.message || 'Generation failed'); }
    finally { setGenLoading(false); }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2} flexWrap="wrap" gap={1}>
        <Typography variant="h4" fontWeight="bold">Timetable</Typography>
        <Box display="flex" gap={1}>
          <Button variant="outlined" startIcon={<AutoAwesomeIcon />} onClick={() => setGenDialog(true)}>
            Generate
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreate}>
            Add Entry
          </Button>
        </Box>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Course</TableCell>
              <TableCell>Lecturer</TableCell>
              <TableCell>Day</TableCell>
              <TableCell>Time</TableCell>
              <TableCell>Room</TableCell>
              <TableCell>Department</TableCell>
              <TableCell>Semester</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {entries.map((e) => (
              <TableRow key={e.id}>
                <TableCell>{e.courseCode} - {e.courseTitle}</TableCell>
                <TableCell>{e.lecturerName || '-'}</TableCell>
                <TableCell><Chip label={e.dayOfWeek} size="small" color="primary" variant="outlined" /></TableCell>
                <TableCell>{e.startTime} - {e.endTime}</TableCell>
                <TableCell>{e.room || '-'}</TableCell>
                <TableCell>{e.departmentName || '-'}</TableCell>
                <TableCell>{e.semester}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => openEdit(e)}><EditIcon /></IconButton>
                  <IconButton onClick={() => handleDelete(e.id)} color="error"><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
            {entries.length === 0 && (
              <TableRow><TableCell colSpan={8} align="center">No timetable entries yet.</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialog} onClose={() => setDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Entry' : 'Add Timetable Entry'}</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} mt={1}>
            <TextField select label="Course" value={form.courseId} onChange={(e) => setForm({ ...form, courseId: e.target.value })} required>
              {courses.map((c) => <MenuItem key={c.id} value={c.id}>{c.courseCode} - {c.title}</MenuItem>)}
            </TextField>
            <TextField select label="Lecturer" value={form.lecturerId} onChange={(e) => setForm({ ...form, lecturerId: e.target.value })}>
              <MenuItem value="">None</MenuItem>
              {lecturers.map((l) => <MenuItem key={l.id} value={l.id}>{l.fullName}</MenuItem>)}
            </TextField>
            <TextField select label="Day" value={form.dayOfWeek} onChange={(e) => setForm({ ...form, dayOfWeek: e.target.value })}>
              {DAYS.map((d) => <MenuItem key={d} value={d}>{d}</MenuItem>)}
            </TextField>
            <TextField label="Start Time" type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} InputLabelProps={{ shrink: true }} />
            <TextField label="End Time" type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} InputLabelProps={{ shrink: true }} />
            <TextField label="Room" value={form.room} onChange={(e) => setForm({ ...form, room: e.target.value })} />
            <TextField label="Location" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
            <TextField select label="Department" value={form.departmentId} onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
              <MenuItem value="">None</MenuItem>
              {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
            <TextField label="Academic Year" value={form.academicYear} onChange={(e) => setForm({ ...form, academicYear: e.target.value })} />
            <TextField select label="Semester" value={form.semester} onChange={(e) => setForm({ ...form, semester: e.target.value })}>
              {SEMESTERS.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialog(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">{editing ? 'Update' : 'Create'}</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={genDialog} onClose={() => setGenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Generate Timetable</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" mb={2}>
            This will auto-assign time slots for all courses in the selected department and semester. Existing entries for this department/semester will be replaced.
          </Typography>
          <Box display="flex" flexDirection="column" gap={2}>
            <TextField select label="Department" value={genForm.departmentId} onChange={(e) => setGenForm({ ...genForm, departmentId: e.target.value })} required>
              {departments.map((d) => <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>)}
            </TextField>
            <TextField label="Academic Year" value={genForm.academicYear} onChange={(e) => setGenForm({ ...genForm, academicYear: e.target.value })} />
            <TextField select label="Semester" value={genForm.semester} onChange={(e) => setGenForm({ ...genForm, semester: e.target.value })}>
              {SEMESTERS.map((s) => <MenuItem key={s} value={s}>{s}</MenuItem>)}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGenDialog(false)}>Cancel</Button>
          <Button onClick={handleGenerate} variant="contained" disabled={genLoading || !genForm.departmentId}>
            {genLoading ? 'Generating...' : 'Generate'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
