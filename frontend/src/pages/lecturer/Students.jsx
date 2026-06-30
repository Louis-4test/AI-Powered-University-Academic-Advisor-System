import { useState, useEffect } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, CircularProgress, Alert,
} from '@mui/material';
import { getMyStudents } from '../../api/lecturers';

export default function LecturerStudents() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getMyStudents()
      .then(res => setStudents(res.data))
      .catch(err => setError(err.response?.data?.error || 'Failed to load students'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Box textAlign="center" py={4}><CircularProgress /></Box>;

  if (error) return <Alert severity="error">{error}</Alert>;

  if (students.length === 0) {
    return (
      <Box>
        <Typography variant="h5" gutterBottom fontWeight="bold">My Students</Typography>
        <Alert severity="info">No students enrolled in your courses yet.</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom fontWeight="bold">My Students</Typography>
      <Typography variant="body2" color="text.secondary" mb={2}>
        {students.length} enrollment{students.length !== 1 ? 's' : ''} across your courses
      </Typography>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Student ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Program</TableCell>
              <TableCell>Department</TableCell>
              <TableCell>Course</TableCell>
              <TableCell>Semester</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {students.map((s, i) => (
              <TableRow key={`${s.studentId}-${s.courseId}-${i}`}>
                <TableCell>{s.studentNumber}</TableCell>
                <TableCell>{s.fullName}</TableCell>
                <TableCell>{s.email}</TableCell>
                <TableCell><Chip size="small" label={`${s.currentLevel}L`} /></TableCell>
                <TableCell>{s.programName || '-'}</TableCell>
                <TableCell>{s.departmentName || '-'}</TableCell>
                <TableCell>{s.courseCode} - {s.courseTitle}</TableCell>
                <TableCell>{s.semester} {s.academicYear}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
