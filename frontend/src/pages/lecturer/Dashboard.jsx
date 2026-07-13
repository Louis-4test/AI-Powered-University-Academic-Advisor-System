import { useState, useEffect } from 'react';
import {
  Grid, Card, CardContent, Typography, CircularProgress, Box, Table,
  TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip,
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import PeopleIcon from '@mui/icons-material/People';
import { getMyLecturerProfile } from '../../api/lecturers';
import { getCoursesByLecturer } from '../../api/courses';

export default function LecturerDashboard() {
  const [profile, setProfile] = useState(null);
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const profRes = await getMyLecturerProfile();
        setProfile(profRes.data);
        const coursesRes = await getCoursesByLecturer(profRes.data.id);
        setCourses(coursesRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <CircularProgress />;
  if (!profile) return <Typography>No profile data found.</Typography>;

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Lecturer Dashboard</Typography>

      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} md={6}>
          <Card sx={{ bgcolor: '#e3f2fd', borderLeft: '4px solid #90caf9', boxShadow: 'none' }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <PeopleIcon color="primary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography variant="h6">{profile.fullName}</Typography>
                  <Typography color="text.secondary">{profile.lecturerId}</Typography>
                  <Typography variant="body2">{profile.email}</Typography>
                  <Typography variant="body2">{profile.departmentName}</Typography>
                </Box>
              </Box>
              {profile.qualification && <Typography mt={1}>Qualification: {profile.qualification}</Typography>}
              {profile.specialization && <Typography>Specialization: {profile.specialization}</Typography>}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card sx={{ bgcolor: '#f3e5f5', borderLeft: '4px solid #ce93d8', boxShadow: 'none' }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <BookIcon color="secondary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography variant="h3" fontWeight="bold" color="primary">{courses.length}</Typography>
                  <Typography color="text.secondary">Assigned Courses</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Typography variant="h5" gutterBottom>My Courses</Typography>
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
