import { useState } from 'react';
import {
  Box, Typography, Button, Card, CardContent, CircularProgress, Alert,
  TextField, Chip, Divider,
} from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import { ingestDocument, queryRag } from '../../api/ai/rag';

export default function Rag() {
  const [file, setFile] = useState(null);
  const [ingesting, setIngesting] = useState(false);
  const [ingestResult, setIngestResult] = useState(null);
  const [ingestError, setIngestError] = useState('');

  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState(null);
  const [querying, setQuerying] = useState(false);
  const [queryError, setQueryError] = useState('');

  const handleIngest = async () => {
    if (!file) return;
    setIngesting(true);
    setIngestError('');
    setIngestResult(null);
    try {
      const res = await ingestDocument(file);
      setIngestResult(res.data);
    } catch (err) {
      setIngestError(err.response?.data?.message || 'Ingestion failed');
    } finally {
      setIngesting(false);
    }
  };

  const handleQuery = async () => {
    if (!question.trim()) return;
    setQuerying(true);
    setQueryError('');
    setAnswer(null);
    try {
      const res = await queryRag(question);
      setAnswer(res.data);
    } catch (err) {
      setQueryError(err.response?.data?.message || 'Query failed');
    } finally {
      setQuerying(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>RAG Knowledge Base</Typography>
      <Typography variant="body1" color="text.secondary" mb={3}>
        Upload university policy documents, course handbooks, or reference materials (PDF or TXT).
        The AI will retrieve relevant content to answer your questions with sources.
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Ingest Documents</Typography>
          <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
            <Button variant="outlined" component="label" startIcon={<CloudUploadIcon />}>
              Choose File
              <input type="file" accept=".pdf,.txt" hidden onChange={(e) => setFile(e.target.files[0])} />
            </Button>
            {file && <Typography>{file.name} ({(file.size / 1024).toFixed(1)} KB)</Typography>}
            <Button variant="contained" onClick={handleIngest} disabled={!file || ingesting}>
              {ingesting ? <CircularProgress size={24} /> : 'Ingest'}
            </Button>
          </Box>
          {ingestError && <Alert severity="error" sx={{ mt: 2 }}>{ingestError}</Alert>}
          {ingestResult && (
            <Alert severity="success" sx={{ mt: 2 }}>
              {ingestResult.fileName} ingested — {ingestResult.chunkCount} chunks stored.
            </Alert>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            <SmartToyIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            Ask from Knowledge Base
          </Typography>
          <Box display="flex" gap={2} alignItems="flex-start">
            <TextField
              fullWidth
              multiline
              minRows={2}
              placeholder="e.g. What are the graduation requirements for Computer Science?"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
            />
            <Button variant="contained" onClick={handleQuery} disabled={!question.trim() || querying} sx={{ minWidth: 100, height: 56 }}>
              {querying ? <CircularProgress size={24} /> : 'Ask'}
            </Button>
          </Box>
          {queryError && <Alert severity="error" sx={{ mt: 2 }}>{queryError}</Alert>}
          {answer && (
            <>
              <Divider sx={{ my: 2 }} />
              <Chip label={`${answer.sourceCount} source(s) retrieved`} size="small" color="primary" sx={{ mb: 2 }} />
              <Typography sx={{ whiteSpace: 'pre-wrap' }}>{answer.answer}</Typography>
            </>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
