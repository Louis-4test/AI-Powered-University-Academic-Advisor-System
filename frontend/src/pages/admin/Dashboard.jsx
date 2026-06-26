import { useState, useEffect } from 'react';
import {
  Grid, Card, CardContent, Typography, CircularProgress, Box,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
} from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import BookIcon from '@mui/icons-material/Book';
import SchoolIcon from '@mui/icons-material/School';
import BusinessIcon from '@mui/icons-material/Business';
import { getStudents, getStatsByLevel } from '../../api/students';
import { getCourses } from '../../api/courses';
import { getLecturers } from '../../api/lecturers';
import { getDepartments } from '../../api/departments';
import { getClassRanking } from '../../api/grades';

export default function AdminDashboard() {
  const [stats, setStats] = useState({ students: 0, courses: 0, lecturers: 0, departments: 0 });
  const [ranking, setRanking] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [sRes, cRes, lRes, dRes, rRes] = await Promise.all([
          getStudents(), getCourses(), getLecturers(), getDepartments(), getClassRanking(),
        ]);
        setStats({ students: sRes.data.length, courses: cRes.data.length, lecturers: lRes.data.length, departments: dRes.data.length });
        setRanking(rRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <CircularProgress />;

  const statCards = [
    { label: 'Students', value: stats.students, icon: <PeopleIcon sx={{ fontSize: 40 }} />, color: '#1565c0' },
    { label: 'Courses', value: stats.courses, icon: <BookIcon sx={{ fontSize: 40 }} />, color: '#2e7d32' },
    { label: 'Lecturers', value: stats.lecturers, icon: <SchoolIcon sx={{ fontSize: 40 }} />, color: '#6a1b9a' },
    { label: 'Departments', value: stats.departments, icon: <BusinessIcon sx={{ fontSize: 40 }} />, color: '#e65100' },
  ];

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Admin Dashboard</Typography>
      <Grid container spacing={3} mb={3}>
        {statCards.map((card) => (
          <Grid item xs={12} sm={6} md={3} key={card.label}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={2}>
                  <Typography color={card.color}>{card.icon}</Typography>
                  <Box>
                    <Typography variant="h4" fontWeight="bold">{card.value}</Typography>
                    <Typography color="text.secondary">{card.label}</Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {ranking.length > 0 && (
        <Box>
          <Typography variant="h5" gutterBottom>Class Ranking</Typography>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Student</TableCell>
                  <TableCell align="right">CGPA</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {ranking.map((r, i) => (
                  <TableRow key={i}>
                    <TableCell>{i + 1}</TableCell>
                    <TableCell>{r.studentName || r.studentId}</TableCell>
                    <TableCell align="right">{r.cgpa?.toFixed(2)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
    </Box>
  );
}
