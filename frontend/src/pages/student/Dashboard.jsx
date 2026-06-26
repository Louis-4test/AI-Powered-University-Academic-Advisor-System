import { useState, useEffect } from 'react';
import {
  Grid, Card, CardContent, Typography, CircularProgress, Box, Chip,
} from '@mui/material';
import SchoolIcon from '@mui/icons-material/School';
import StarsIcon from '@mui/icons-material/Stars';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { getMyProfile } from '../../api/students';
import { getCgpa, getPerformanceTrend } from '../../api/grades';

export default function StudentDashboard() {
  const [profile, setProfile] = useState(null);
  const [cgpa, setCgpa] = useState(null);
  const [trend, setTrend] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const [profRes, cgpaRes, trendRes] = await Promise.all([
          getMyProfile(),
          getCgpa(null).catch(() => ({ data: { cgpa: 0 } })),
          getPerformanceTrend(null).catch(() => ({ data: [] })),
        ]);
        const studentId = profRes.data.id;
        const [cgpaData, trendData] = await Promise.all([
          getCgpa(studentId),
          getPerformanceTrend(studentId),
        ]);
        setProfile(profRes.data);
        setCgpa(cgpaData.data.cgpa);
        setTrend(trendData.data);
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
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        Student Dashboard
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <SchoolIcon color="primary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">Student ID</Typography>
                  <Typography variant="h6">{profile.studentId}</Typography>
                </Box>
              </Box>
              <Typography mt={1}><strong>Name:</strong> {profile.fullName}</Typography>
              <Typography><strong>Email:</strong> {profile.email}</Typography>
              <Typography><strong>Program:</strong> {profile.programName}</Typography>
              <Typography><strong>Level:</strong> {profile.currentLevel}</Typography>
              <Chip label={profile.status} color={profile.status === 'ACTIVE' ? 'success' : 'warning'} size="small" sx={{ mt: 1 }} />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <StarsIcon color="secondary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">CGPA</Typography>
                  <Typography variant="h3" fontWeight="bold" color="primary">
                    {cgpa?.toFixed(2) || 'N/A'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <TrendingUpIcon color="success" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">Department</Typography>
                  <Typography variant="h6">{profile.departmentName || 'N/A'}</Typography>
                </Box>
              </Box>
              <Typography mt={1}><strong>Enrolled:</strong> {profile.enrollmentYear}</Typography>
            </CardContent>
          </Card>
        </Grid>

        {trend.length > 0 && (
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Performance Trend</Typography>
                {trend.map((t, i) => (
                  <Box key={i} display="flex" justifyContent="space-between" py={0.5}>
                    <Typography>{t.semester} {t.academicYear}</Typography>
                    <Typography fontWeight="bold">GPA: {t.gpa?.toFixed(2)}</Typography>
                  </Box>
                ))}
              </CardContent>
            </Card>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
