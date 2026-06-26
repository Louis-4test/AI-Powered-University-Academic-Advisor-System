import { useState } from 'react';
import {
  Box, Typography, TextField, Button, Card, CardContent,
  CircularProgress, Paper, Avatar,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import PersonIcon from '@mui/icons-material/Person';
import { askChatbot } from '../../api/ai/chatbot';

export default function Chatbot() {
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleSend = async () => {
    if (!question.trim()) return;
    const userMsg = { role: 'user', text: question };
    setMessages((prev) => [...prev, userMsg]);
    setLoading(true);
    setQuestion('');
    try {
      const res = await askChatbot(question);
      setMessages((prev) => [...prev, { role: 'ai', text: res.data.answer }]);
    } catch {
      setMessages((prev) => [...prev, { role: 'ai', text: 'Sorry, I encountered an error.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>Academic Chatbot</Typography>
      <Typography variant="body1" color="text.secondary" mb={2}>
        Ask questions about courses, graduation requirements, programming concepts, and more.
      </Typography>

      <Card sx={{ mb: 2 }}>
        <CardContent sx={{ height: 400, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 2 }}>
          {messages.length === 0 && (
            <Box display="flex" alignItems="center" justifyContent="center" height="100%">
              <Typography color="text.secondary">Ask something to get started!</Typography>
            </Box>
          )}
          {messages.map((msg, i) => (
            <Box key={i} display="flex" gap={1} justifyContent={msg.role === 'user' ? 'flex-end' : 'flex-start'}>
              {msg.role === 'ai' && <Avatar sx={{ bgcolor: 'primary.main' }}><SmartToyIcon /></Avatar>}
              <Paper sx={{ p: 2, maxWidth: '70%', bgcolor: msg.role === 'user' ? 'primary.light' : 'grey.100' }}>
                <Typography>{msg.text}</Typography>
              </Paper>
              {msg.role === 'user' && <Avatar sx={{ bgcolor: 'secondary.main' }}><PersonIcon /></Avatar>}
            </Box>
          ))}
          {loading && (
            <Box display="flex" gap={1}>
              <Avatar sx={{ bgcolor: 'primary.main' }}><SmartToyIcon /></Avatar>
              <CircularProgress size={24} />
            </Box>
          )}
        </CardContent>
      </Card>

      <Box display="flex" gap={1}>
        <TextField
          fullWidth
          placeholder="Type your question..."
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSend()}
          disabled={loading}
        />
        <Button variant="contained" onClick={handleSend} disabled={loading || !question.trim()}>
          <SendIcon />
        </Button>
      </Box>
    </Box>
  );
}
