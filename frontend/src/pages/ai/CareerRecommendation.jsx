import { useState } from 'react';
import {
  Box, Typography, TextField, Button, Card, CardContent,
  CircularProgress, Alert, Chip, Grid, LinearProgress,
} from '@mui/material';
import { getCareerRecommendations } from '../../api/ai/careerRecommendation';

export default function CareerRecommendation() {
  const [studentId, setStudentId] = useState('');
  const [skills, setSkills] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleRecommend = async () => {
    if (!studentId) return;
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const additionalSkills = skills ? skills.split(',').map((s) => s.trim()).filter(Boolean) : undefined;
      const res = await getCareerRecommendations(Number(studentId), additionalSkills);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Recommendation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Career Recommendation</Typography>
      <Typography variant="body1" color="text.secondary" mb={2}>
        Get AI-powered career recommendations based on your academic profile and skills.
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
            <TextField label="Student ID" type="number" value={studentId} onChange={(e) => setStudentId(e.target.value)} required />
            <TextField label="Additional Skills (comma-separated)" value={skills} onChange={(e) => setSkills(e.target.value)} sx={{ minWidth: 300 }} placeholder="e.g. Python, Machine Learning, Leadership" />
            <Button variant="contained" onClick={handleRecommend} disabled={loading || !studentId}>
              {loading ? <CircularProgress size={24} /> : 'Get Recommendations'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {result && (
        <Box>
          <Typography variant="h6" gutterBottom>
            Career Recommendations for {result.studentName}
          </Typography>
          <Grid container spacing={2}>
            {result.careers?.map((career, i) => (
              <Grid item xs={12} md={6} key={i}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" fontWeight="bold">{career.careerTitle}</Typography>
                    <Box display="flex" alignItems="center" gap={1} mt={1}>
                      <Typography variant="body2" color="text.secondary">Fit Score:</Typography>
                      <LinearProgress
                        variant="determinate"
                        value={career.fitScore}
                        sx={{ flexGrow: 1, height: 8, borderRadius: 4 }}
                      />
                      <Typography variant="body2" fontWeight="bold">{career.fitScore}%</Typography>
                    </Box>
                    <Typography variant="body2" mt={1}>{career.rationale}</Typography>
                    {career.suggestedNextSteps?.length > 0 && (
                      <Box mt={1}>
                        <Typography variant="body2" fontWeight="bold">Next Steps:</Typography>
                        <Box display="flex" gap={0.5} flexWrap="wrap" mt={0.5}>
                          {career.suggestedNextSteps.map((step, j) => (
                            <Chip key={j} label={step} size="small" variant="outlined" />
                          ))}
                        </Box>
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}
    </Box>
  );
}
