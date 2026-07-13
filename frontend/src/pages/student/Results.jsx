import { useState, useEffect } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, Paper, CircularProgress, Card, CardContent, Grid,
} from '@mui/material';
import { getMyProfile } from '../../api/students';
import { getStudentGrades, getCgpa } from '../../api/grades';

export default function StudentResults() {
  const [grades, setGrades] = useState([]);
  const [cgpa, setCgpa] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const prof = await getMyProfile();
        const sid = prof.data.id;
        const [gradesRes, cgpaRes] = await Promise.all([
          getStudentGrades(sid),
          getCgpa(sid),
        ]);
        setGrades(gradesRes.data);
        setCgpa(cgpaRes.data.cgpa);
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
      <Typography variant="h4" fontWeight="bold" gutterBottom>My Results</Typography>

      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ bgcolor: '#e3f2fd', borderLeft: '4px solid #90caf9', boxShadow: 'none' }}><CardContent>
            <Typography color="text.secondary" variant="body2">CGPA</Typography>
            <Typography variant="h4" fontWeight="bold" color="primary">{cgpa?.toFixed(2) || 'N/A'}</Typography>
          </CardContent></Card>
        </Grid>
      </Grid>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Course</TableCell>
              <TableCell>Semester</TableCell>
              <TableCell align="right">Att.</TableCell>
              <TableCell align="right">Assign.</TableCell>
              <TableCell align="right">Proj.</TableCell>
              <TableCell align="right">Tests</TableCell>
              <TableCell align="right">Exam</TableCell>
              <TableCell align="right">Total</TableCell>
              <TableCell align="right">Grade</TableCell>
              <TableCell align="right">GP</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {grades.map((g) => (
              <TableRow key={g.id}>
                <TableCell>{g.courseCode} - {g.courseTitle}</TableCell>
                <TableCell>{g.semester} {g.academicYear}</TableCell>
                <TableCell align="right">{g.attendance}</TableCell>
                <TableCell align="right">{g.assignments}</TableCell>
                <TableCell align="right">{g.projects}</TableCell>
                <TableCell align="right">{g.tests}</TableCell>
                <TableCell align="right">{g.exams}</TableCell>
                <TableCell align="right"><strong>{g.totalScore?.toFixed(1)}</strong></TableCell>
                <TableCell align="right"><strong>{g.letterGrade}</strong></TableCell>
                <TableCell align="right">{g.gradePoint?.toFixed(1)}</TableCell>
              </TableRow>
            ))}
            {grades.length === 0 && (
              <TableRow><TableCell colSpan={10} align="center">No grades recorded yet.</TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
