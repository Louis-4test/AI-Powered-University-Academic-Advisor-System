import { useState } from 'react';
import {
  Box, Typography, Button, Card, CardContent, CircularProgress, Alert, Chip,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { analyzeDocument } from '../../api/ai/researchAssistant';

export default function ResearchAssistant() {
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);
    setError('');
    setResult(null);
    try {
      const res = await analyzeDocument(file);
      setResult(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Analysis failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Research Assistant</Typography>
      <Typography variant="body1" color="text.secondary" mb={2}>
        Upload a research paper (PDF) to get AI-generated summary, key findings, research gaps, and future work.
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
            <Button variant="outlined" component="label" startIcon={<CloudUploadIcon />}>
              Choose PDF
              <input type="file" accept=".pdf" hidden onChange={(e) => setFile(e.target.files[0])} />
            </Button>
            {file && <Typography>{file.name} ({(file.size / 1024).toFixed(1)} KB)</Typography>}
            <Button variant="contained" onClick={handleUpload} disabled={!file || loading}>
              {loading ? <CircularProgress size={24} /> : 'Analyze'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {result && (
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>{result.fileName}</Typography>
            <Chip label={`${result.extractedCharacterCount} chars extracted`} size="small" sx={{ mb: 2 }} />
            <Box mb={2}>
              <Typography variant="subtitle1" fontWeight="bold">Summary</Typography>
              <Typography>{result.summary}</Typography>
            </Box>
            <Box mb={2}>
              <Typography variant="subtitle1" fontWeight="bold">Key Findings</Typography>
              <Typography>{result.keyFindings}</Typography>
            </Box>
            <Box mb={2}>
              <Typography variant="subtitle1" fontWeight="bold">Research Gaps</Typography>
              <Typography>{result.researchGaps}</Typography>
            </Box>
            <Box>
              <Typography variant="subtitle1" fontWeight="bold">Future Work</Typography>
              <Typography>{result.futureWork}</Typography>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}
