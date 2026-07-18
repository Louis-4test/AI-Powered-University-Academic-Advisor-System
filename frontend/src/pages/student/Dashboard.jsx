import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Typography, Button, Grid, Card, CardContent, CardActionArea,
  CircularProgress, Avatar, Stack, Chip, Divider,
} from '@mui/material';
import BookIcon from '@mui/icons-material/Book';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import AssignmentIcon from '@mui/icons-material/Assignment';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';
import SchoolIcon from '@mui/icons-material/School';
import StarsIcon from '@mui/icons-material/Stars';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import ChatIcon from '@mui/icons-material/Chat';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import WorkIcon from '@mui/icons-material/Work';
import PsychologyIcon from '@mui/icons-material/Psychology';
import MenuBookIcon from '@mui/icons-material/MenuBook';
import heroImg from '../../assets/images.jpeg';
import { getMyProfile } from '../../api/students';
import { getCgpa, getPerformanceTrend } from '../../api/grades';

const features = [
  {
    title: 'My Courses',
    desc: 'View enrolled courses, track progress and manage your academic load.',
    icon: <BookIcon sx={{ fontSize: 36 }} />,
    path: '/student/courses',
    color: '#1565c0',
    bg: '#e3f2fd',
    border: '#90caf9',
  },
  {
    title: 'Timetable',
    desc: 'Access your weekly class schedule and never miss a lecture.',
    icon: <CalendarMonthIcon sx={{ fontSize: 36 }} />,
    path: '/student/timetable',
    color: '#2e7d32',
    bg: '#e8f5e9',
    border: '#a5d6a7',
  },
  {
    title: 'Results',
    desc: 'Check grades, CGPA breakdown and performance trends.',
    icon: <AssignmentIcon sx={{ fontSize: 36 }} />,
    path: '/student/results',
    color: '#c62828',
    bg: '#ffebee',
    border: '#ef9a9a',
  },
  {
    title: 'AI Advisor',
    desc: 'Get AI-powered academic guidance, career advice and study plans.',
    icon: <SmartToyIcon sx={{ fontSize: 36 }} />,
    path: '/ai',
    color: '#6a1b9a',
    bg: '#f3e5f5',
    border: '#ce93d8',
  },
];

const aiTools = [
  { title: 'Chatbot', desc: 'Ask academic questions', icon: <ChatIcon />, path: '/ai/chatbot', color: '#1565c0' },
  { title: 'Risk Prediction', desc: 'Identify at-risk courses', icon: <WarningAmberIcon />, path: '/ai/risk-prediction', color: '#e65100' },
  { title: 'Career Path', desc: 'Personalized recommendations', icon: <WorkIcon />, path: '/ai/career', color: '#2e7d32' },
  { title: 'Research Assistant', desc: 'Analyze papers with AI', icon: <PsychologyIcon />, path: '/ai/research-assistant', color: '#6a1b9a' },
  { title: 'Exam Prep', desc: 'AI-generated practice questions', icon: <MenuBookIcon />, path: '/ai/exam-generator', color: '#c62828' },
  { title: 'Knowledge Base', desc: 'RAG-powered document search', icon: <AutoAwesomeIcon />, path: '/ai/rag', color: '#00838f' },
];

export default function StudentDashboard() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [cgpa, setCgpa] = useState(null);
  const [trend, setTrend] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const profRes = await getMyProfile();
        const studentId = profRes.data.id;
        setProfile(profRes.data);

        const [cgpaData, trendData] = await Promise.all([
          getCgpa(studentId),
          getPerformanceTrend(studentId),
        ]);
        setCgpa(cgpaData.data.cgpa);
        setTrend(trendData.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (!profile) {
    return <Typography>No profile data found.</Typography>;
  }

  return (
    <Box>
      {/* Hero Section */}
      <Box
        sx={{
          display: 'flex',
          borderRadius: 3,
          overflow: 'hidden',
          mb: 4,
          background: 'linear-gradient(135deg, #e3f2fd 0%, #ede7f6 100%)',
          minHeight: { xs: 'auto', md: 380 },
        }}
      >
        <Box
          sx={{
            flex: 1,
            p: { xs: 4, md: 6 },
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
          }}
        >
          <Chip
            icon={<AutoAwesomeIcon />}
            label="AI-Powered Academic Advisor"
            size="small"
            color="primary"
            variant="outlined"
            sx={{ alignSelf: 'flex-start', mb: 2 }}
          />
          <Typography
            variant="h3"
            fontWeight={800}
            gutterBottom
            sx={{
              background: 'linear-gradient(135deg, #1565c0, #7b1fa2)',
              backgroundClip: 'text',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
              lineHeight: 1.2,
            }}
          >
            Navigate Your Academic Journey with AI
          </Typography>
          <Typography variant="h6" color="text.secondary" mb={4} sx={{ maxWidth: 460 }}>
            Personalized course recommendations, performance insights, and intelligent
            guidance to help you succeed every step of the way.
          </Typography>
          <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
            <Button
              variant="contained"
              size="large"
              startIcon={<RocketLaunchIcon />}
              onClick={() => navigate('/ai')}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              Explore AI Tools
            </Button>
            <Button
              variant="outlined"
              size="large"
              startIcon={<BookIcon />}
              onClick={() => navigate('/student/courses')}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              My Courses
            </Button>
          </Stack>
        </Box>
        <Box
          component="img"
          src={heroImg}
          alt="Academic Advisor"
          sx={{
            width: { xs: '100%', md: '45%' },
            height: { xs: 240, md: 'auto' },
            objectFit: 'cover',
            display: 'block',
          }}
        />
      </Box>

      {/* Quick Access Features */}
      <Typography variant="h5" fontWeight="bold" gutterBottom>
        Quick Access
      </Typography>
      <Grid container spacing={3} sx={{ mb: 5 }}>
        {features.map((f) => (
          <Grid item xs={12} sm={6} md={3} key={f.title}>
            <Card
              sx={{
                height: '100%',
                borderRadius: 2,
                bgcolor: f.bg,
                borderLeft: `4px solid ${f.border}`,
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': { transform: 'translateY(-4px)', boxShadow: 4 },
              }}
            >
              <CardActionArea onClick={() => navigate(f.path)} sx={{ p: 0.5 }}>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1.5} mb={1.5}>
                    <Avatar sx={{ bgcolor: f.color, width: 44, height: 44 }}>
                      {f.icon}
                    </Avatar>
                    <Typography variant="h6" fontWeight="bold">
                      {f.title}
                    </Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {f.desc}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Profile Summary + CGPA Row */}
      <Grid container spacing={3} sx={{ mb: 5 }}>
        <Grid item xs={12} md={4}>
          <Card sx={{ bgcolor: '#e3f2fd', borderLeft: '4px solid #90caf9', boxShadow: 'none', height: '100%' }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <SchoolIcon color="primary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">Student ID</Typography>
                  <Typography variant="h6">{profile.studentId}</Typography>
                </Box>
              </Box>
              <Typography mt={1}><strong>Name:</strong> {profile.fullName}</Typography>
              <Typography><strong>Program:</strong> {profile.programName}</Typography>
              <Typography><strong>Level:</strong> {profile.currentLevel}</Typography>
              <Chip
                label={profile.status}
                color={profile.status === 'ACTIVE' ? 'success' : 'warning'}
                size="small"
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ bgcolor: '#f3e5f5', borderLeft: '4px solid #ce93d8', boxShadow: 'none', height: '100%' }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <StarsIcon color="secondary" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">Cumulative GPA</Typography>
                  <Typography variant="h3" fontWeight="bold" color="primary">
                    {cgpa?.toFixed(2) || 'N/A'}
                  </Typography>
                </Box>
              </Box>
              {trend.length > 0 && (
                <Box mt={2}>
                  <Typography variant="body2" color="text.secondary" mb={0.5}>Recent Trend</Typography>
                  {trend.slice(0, 2).map((t, i) => (
                    <Box key={i} display="flex" justifyContent="space-between" py={0.3}>
                      <Typography variant="body2">{t.semester} {t.academicYear}</Typography>
                      <Typography variant="body2" fontWeight="bold">GPA: {t.gpa?.toFixed(2)}</Typography>
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ bgcolor: '#e8f5e9', borderLeft: '4px solid #a5d6a7', boxShadow: 'none', height: '100%' }}>
            <CardContent>
              <Box display="flex" alignItems="center" gap={2}>
                <TrendingUpIcon color="success" sx={{ fontSize: 40 }} />
                <Box>
                  <Typography color="text.secondary" variant="body2">Department</Typography>
                  <Typography variant="h6">{profile.departmentName || 'N/A'}</Typography>
                </Box>
              </Box>
              <Typography mt={1}><strong>Enrolled:</strong> {profile.enrollmentYear}</Typography>
              <Typography><strong>Email:</strong> {profile.email}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* AI Tools Section */}
      <Box mb={2}>
        <Divider sx={{ mb: 4 }} />
        <Typography variant="h5" fontWeight="bold" gutterBottom>
          AI-Powered Tools
        </Typography>
        <Typography variant="body1" color="text.secondary" mb={3}>
          Leverage artificial intelligence to enhance your academic experience.
        </Typography>
      </Box>
      <Grid container spacing={2}>
        {aiTools.map((tool) => (
          <Grid item xs={6} sm={4} md={2} key={tool.title}>
            <Card
              sx={{
                height: '100%',
                borderRadius: 2,
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': { transform: 'translateY(-4px)', boxShadow: 4 },
              }}
            >
              <CardActionArea onClick={() => navigate(tool.path)} sx={{ p: 2 }}>
                <CardContent sx={{ textAlign: 'center', p: '12px !important' }}>
                  <Avatar
                    sx={{
                      bgcolor: `${tool.color}15`,
                      color: tool.color,
                      width: 48,
                      height: 48,
                      mx: 'auto',
                      mb: 1,
                    }}
                  >
                    {tool.icon}
                  </Avatar>
                  <Typography variant="subtitle2" fontWeight="bold">
                    {tool.title}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {tool.desc}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
}
