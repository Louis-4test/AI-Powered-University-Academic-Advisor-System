import { useNavigate } from 'react-router-dom';
import { Grid, Card, CardContent, Typography, CardActionArea } from '@mui/material';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import WarningIcon from '@mui/icons-material/Warning';
import WorkIcon from '@mui/icons-material/Work';
import PsychologyIcon from '@mui/icons-material/Psychology';
import AssignmentIcon from '@mui/icons-material/Assignment';
import MenuBookIcon from '@mui/icons-material/MenuBook';

const tools = [
  { title: 'Academic Chatbot', desc: 'Ask academic questions and get AI-powered answers', icon: <SmartToyIcon sx={{ fontSize: 48 }} />, path: '/ai/chatbot', color: '#1565c0' },
  { title: 'Risk Prediction', desc: 'Predict student performance and identify at-risk students', icon: <WarningIcon sx={{ fontSize: 48 }} />, path: '/ai/risk-prediction', color: '#e65100' },
  { title: 'Career Recommendation', desc: 'Get personalized career recommendations', icon: <WorkIcon sx={{ fontSize: 48 }} />, path: '/ai/career', color: '#2e7d32' },
  { title: 'Research Assistant', desc: 'Analyze research papers with AI', icon: <PsychologyIcon sx={{ fontSize: 48 }} />, path: '/ai/research-assistant', color: '#6a1b9a' },
  { title: 'Exam Generator', desc: 'Generate exam questions from any topic', icon: <AssignmentIcon sx={{ fontSize: 48 }} />, path: '/ai/exam-generator', color: '#c62828' },
  { title: 'RAG Knowledge Base', desc: 'Upload documents and ask AI-powered questions with source citations', icon: <MenuBookIcon sx={{ fontSize: 48 }} />, path: '/ai/rag', color: '#1565c0' },
];

export default function AiOverview() {
  const navigate = useNavigate();

  return (
    <div>
      <Typography variant="h4" fontWeight="bold" gutterBottom>AI Tools</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Leverage artificial intelligence to enhance academic advising, research, and teaching.
      </Typography>
      <Grid container spacing={3}>
        {tools.map((tool) => (
          <Grid item xs={12} sm={6} md={4} key={tool.title}>
            <Card>
              <CardActionArea onClick={() => navigate(tool.path)} sx={{ p: 2 }}>
                <CardContent>
                  <Typography color={tool.color} align="center">{tool.icon}</Typography>
                  <Typography variant="h6" fontWeight="bold" align="center" mt={1}>{tool.title}</Typography>
                  <Typography variant="body2" color="text.secondary" align="center">{tool.desc}</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </div>
  );
}
