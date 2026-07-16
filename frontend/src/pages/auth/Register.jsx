import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert,
  CircularProgress, MenuItem, InputAdornment, IconButton,
} from '@mui/material';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { useAuth } from '../../context/AuthContext';

export default function Register() {
  const [form, setForm] = useState({ fullName: '', email: '', password: '', role: 'STUDENT' });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const validate = () => {
    const newErrors = {};
    if (!form.fullName.trim()) newErrors.fullName = 'Full name is required';
    if (!form.email.trim()) newErrors.email = 'Email is required';
    if (!form.password.trim()) newErrors.password = 'Password is required';
    else if (form.password.length < 6) newErrors.password = 'Password must be at least 6 characters';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;
    setLoading(true);
    try {
      const data = await register(form.fullName, form.email, form.password, form.role);
      const role = data.role;
      if (role === 'ADMIN') navigate('/admin');
      else if (role === 'STUDENT') navigate('/student');
      else if (role === 'LECTURER') navigate('/lecturer');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', bgcolor: '#f5f5f5' }}>
      <Card sx={{ maxWidth: 420, width: '100%', mx: 2 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h4" fontWeight="bold" gutterBottom align="center" color="primary">
            Create Account
          </Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Box component="form" onSubmit={handleSubmit}>
            <TextField fullWidth label="Full Name" name="fullName" margin="normal" required value={form.fullName} onChange={handleChange} error={!!errors.fullName} helperText={errors.fullName} />
            <TextField fullWidth label="Email" name="email" type="email" margin="normal" required value={form.email} onChange={handleChange} error={!!errors.email} helperText={errors.email} />
            <TextField
              fullWidth label="Password" name="password" margin="normal" required
              type={showPassword ? 'text' : 'password'}
              value={form.password} onChange={handleChange}
              error={!!errors.password} helperText={errors.password}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPassword(!showPassword)} edge="end" size="small">
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <TextField fullWidth select label="Role" name="role" margin="normal" value={form.role} onChange={handleChange}>
              <MenuItem value="STUDENT">Student</MenuItem>
              <MenuItem value="LECTURER">Lecturer</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </TextField>
            <Button fullWidth type="submit" variant="contained" size="large" disabled={loading} sx={{ mt: 2, py: 1.5 }}>
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Register'}
            </Button>
          </Box>
          <Box textAlign="center" mt={2}>
            <Button onClick={() => navigate('/login')}>Already have an account? Sign in</Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
