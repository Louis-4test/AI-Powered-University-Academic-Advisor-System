import { useState, useEffect } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, CircularProgress, Chip,
} from '@mui/material';
import { getMyLecturerProfile } from '../../api/lecturers';
import { getCoursesByLecturer } from '../../api/courses';

export default function LecturerCourses() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const prof = await getMyLecturerProfile();
        const res = await getCoursesByLecturer(prof.data.id);
        setCourses(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>My Courses</Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Code</TableCell>
              <TableCell>Title</TableCell>
              <TableCell>Level</TableCell>
              <TableCell>Semester</TableCell>
              <TableCell>Credits</TableCell>
              <TableCell>Enrollments</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {courses.map((c) => (
              <TableRow key={c.id}>
                <TableCell>{c.courseCode}</TableCell>
                <TableCell>{c.title}</TableCell>
                <TableCell>{c.level}</TableCell>
                <TableCell>{c.semester}</TableCell>
                <TableCell>{c.creditHours}</TableCell>
                <TableCell>{c.enrollmentCount}</TableCell>
                <TableCell>
                  <Chip label={c.status} size="small" color={c.status === 'ACTIVE' ? 'success' : 'default'} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
