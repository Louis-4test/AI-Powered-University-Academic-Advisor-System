import { useState } from 'react';
import {
  Box, Typography, TextField, Button, Card, CardContent,
  CircularProgress, Alert, Chip, LinearProgress, Grid,
} from '@mui/material';
import { getStudentGrades } from '../../api/grades';
import { predictRisk } from '../../api/ai/riskPrediction';

export default function RiskPrediction() {
  const [studentId, setStudentId] = useState('');
  const [grades, setGrades] = useState([]);
  const [selectedGrade, setSelectedGrade] = useState(null);
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [predicting, setPredicting] = useState(false);
  const [error, setError] = useState('');

  const loadGrades = async () => {
    if (!studentId) return;
    setLoading(true);
    setError('');
    setPrediction(null);
    setSelectedGrade(null);
    try {
      const res = await getStudentGrades(Number(studentId));
      setGrades(res.data);
    } catch {
      setError('Student not found');
    } finally {
      setLoading(false);
    }
  };

  const handlePredict = async (grade) => {
    setSelectedGrade(grade);
    setPredicting(true);
    try {
      const res = await predictRisk(grade.id);
      setPrediction(res.data);
    } catch {
      setError('Prediction failed');
    } finally {
      setPredicting(false);
    }
  };

  const riskColor = (category) => {
    switch (category) {
      case 'EXCELLENT_PERFORMANCE': return 'success';
      case 'LIKELY_TO_PASS': return 'info';
      case 'AT_RISK': return 'warning';
      case 'LIKELY_TO_FAIL': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Risk Prediction</Typography>
      <Typography variant="body1" color="text.secondary" mb={2}>
        Analyze a student's performance data to predict academic risk.
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" gap={2} alignItems="center">
            <TextField label="Student ID" type="number" value={studentId} onChange={(e) => setStudentId(e.target.value)} />
            <Button variant="contained" onClick={loadGrades} disabled={loading || !studentId}>
              {loading ? <CircularProgress size={24} /> : 'Load Grades'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {grades.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>Select a course grade to analyze:</Typography>
            <Box display="flex" gap={1} flexWrap="wrap">
              {grades.map((g) => (
                <Chip
                  key={g.id}
                  label={`${g.courseCode} (${g.letterGrade})`}
                  onClick={() => handlePredict(g)}
                  color={selectedGrade?.id === g.id ? 'primary' : 'default'}
                  variant={selectedGrade?.id === g.id ? 'filled' : 'outlined'}
                />
              ))}
            </Box>
          </CardContent>
        </Card>
      )}

      {predicting && <CircularProgress sx={{ display: 'block', mx: 'auto' }} />}

      {prediction && (
        <Card>
          <CardContent>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Typography variant="h6">{prediction.studentName}</Typography>
                <Typography color="text.secondary">{prediction.courseCode}</Typography>
                <Box mt={1}>
                  <Typography variant="body2">Attendance: {prediction.attendance}%</Typography>
                  <Typography variant="body2">Assignments: {prediction.assignments}%</Typography>
                  <Typography variant="body2">Projects: {prediction.projects}%</Typography>
                  <Typography variant="body2">Tests: {prediction.tests}%</Typography>
                  <Typography variant="body2">Exams: {prediction.exams}%</Typography>
                </Box>
              </Grid>
              <Grid item xs={12} md={6}>
                <Box textAlign="center">
                  <Typography variant="body2" color="text.secondary">Risk Category</Typography>
                  <Chip label={prediction.category?.replace(/_/g, ' ')} color={riskColor(prediction.category)} sx={{ my: 1 }} />
                  <Typography variant="body2" color="text.secondary">Risk Score: {prediction.riskScore}/100</Typography>
                  <LinearProgress
                    variant="determinate"
                    value={prediction.riskScore}
                    color={prediction.riskScore > 60 ? 'error' : prediction.riskScore > 30 ? 'warning' : 'success'}
                    sx={{ height: 10, borderRadius: 5, my: 1 }}
                  />
                </Box>
              </Grid>
              <Grid item xs={12}>
                <Box mt={2}>
                  <Typography variant="subtitle2" fontWeight="bold">Explanation</Typography>
                  <Typography variant="body2">{prediction.explanation}</Typography>
                </Box>
                <Box mt={1}>
                  <Typography variant="subtitle2" fontWeight="bold">Recommendation</Typography>
                  <Typography variant="body2">{prediction.recommendation}</Typography>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
