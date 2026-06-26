import { useState } from 'react';
import {
  Box, Typography, TextField, Button, MenuItem, Card, CardContent,
  CircularProgress, Alert,
} from '@mui/material';
import { generateExam } from '../../api/ai/examGenerator';

export default function ExamGeneratorPage() {
  const [form, setForm] = useState({ topic: '', difficulty: 'INTERMEDIATE', questionCount: 10 });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const res = await generateExam(form.topic, form.difficulty, form.questionCount);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Generation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Exam Generator</Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSubmit} display="flex" gap={2} flexWrap="wrap" alignItems="end">
            <TextField label="Topic" name="topic" value={form.topic} onChange={(e) => setForm({ ...form, topic: e.target.value })} required sx={{ minWidth: 250 }} />
            <TextField select label="Difficulty" value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })} sx={{ minWidth: 150 }}>
              <MenuItem value="BEGINNER">Beginner</MenuItem>
              <MenuItem value="INTERMEDIATE">Intermediate</MenuItem>
              <MenuItem value="ADVANCED">Advanced</MenuItem>
            </TextField>
            <TextField label="Questions" type="number" value={form.questionCount} onChange={(e) => setForm({ ...form, questionCount: Number(e.target.value) })} sx={{ minWidth: 120 }} inputProps={{ min: 1, max: 50 }} />
            <Button type="submit" variant="contained" disabled={loading} sx={{ height: 56 }}>
              {loading ? <CircularProgress size={24} /> : 'Generate'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {result && (
        <Box>
          <Typography variant="h5" gutterBottom>{result.topic} - {result.difficulty}</Typography>

          {result.mcqs?.length > 0 && (
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom>Multiple Choice Questions</Typography>
                {result.mcqs.map((q, i) => (
                  <Box key={i} mb={1.5}>
                    <Typography><strong>{i + 1}. </strong>{q.question}</Typography>
                    <Box ml={2}>{q.options?.map((opt, j) => (
                      <Typography key={j} variant="body2" color={opt === q.correctAnswer ? 'success.main' : 'text.secondary'}>{opt} {opt === q.correctAnswer ? '✓' : ''}</Typography>
                    ))}</Box>
                    <Typography variant="caption" color="text.secondary">{q.explanation}</Typography>
                  </Box>
                ))}
              </CardContent>
            </Card>
          )}

          {result.theoryQuestions?.length > 0 && (
            <Card sx={{ mb: 2 }}><CardContent>
              <Typography variant="h6" gutterBottom>Theory Questions</Typography>
              {result.theoryQuestions.map((q, i) => (
                <Box key={i} mb={1}><Typography><strong>{i + 1}. </strong>{q.question}</Typography>{q.guidanceForGrading && <Typography variant="caption" color="text.secondary">Grading: {q.guidanceForGrading}</Typography>}</Box>
              ))}
            </CardContent></Card>
          )}

          {result.practicalQuestions?.length > 0 && (
            <Card sx={{ mb: 2 }}><CardContent>
              <Typography variant="h6" gutterBottom>Practical Questions</Typography>
              {result.practicalQuestions.map((q, i) => (
                <Box key={i} mb={1}><Typography><strong>{i + 1}. </strong>{q.question}</Typography>{q.expectedApproach && <Typography variant="caption" color="text.secondary">Approach: {q.expectedApproach}</Typography>}</Box>
              ))}
            </CardContent></Card>
          )}

          {result.caseStudies?.length > 0 && (
            <Card sx={{ mb: 2 }}><CardContent>
              <Typography variant="h6" gutterBottom>Case Studies</Typography>
              {result.caseStudies.map((cs, i) => (
                <Box key={i} mb={1}><Typography><strong>{i + 1}. </strong>Scenario: {cs.scenario}</Typography><Typography variant="body2">Question: {cs.question}</Typography></Box>
              ))}
            </CardContent></Card>
          )}
        </Box>
      )}
    </Box>
  );
}
