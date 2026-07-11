import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, CircularProgress,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(email, password);
      const role = data.role;
      if (role === 'ADMIN') navigate('/admin');
      else if (role === 'STUDENT') navigate('/student');
      else if (role === 'LECTURER') navigate('/lecturer');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', bgcolor: '#f5f5f5' }}>
      <Card sx={{ maxWidth: 420, width: '100%', mx: 2 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" fontWeight="bold" gutterBottom align="center" color="primary">
            Academic Advisor
          </Typography>
          <Typography variant="body2" color="text.secondary" align="center" mb={3}>
            Sign in to your account
          </Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Box component="form" onSubmit={handleSubmit}>
            <TextField
              fullWidth label="Email" type="email" margin="normal" required
              value={email} onChange={(e) => setEmail(e.target.value)}
            />
            <TextField
              fullWidth label="Password" type="password" margin="normal" required
              value={password} onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              fullWidth type="submit" variant="contained" size="large"
              disabled={loading} sx={{ mt: 2, py: 1.5 }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign In'}
            </Button>
          </Box>
          <Box textAlign="center" mt={1}>
            <Button onClick={() => navigate('/forgot-password')} size="small">Forgot Password?</Button>
          </Box>
          <Box textAlign="center" mt={1}>
            <Button onClick={() => navigate('/register')}>Don't have an account? Register</Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
