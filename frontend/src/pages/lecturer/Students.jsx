import { useState, useEffect, useMemo } from 'react';
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, CircularProgress, Alert,
  Collapse, Avatar,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import BusinessIcon from '@mui/icons-material/Business';
import SchoolIcon from '@mui/icons-material/School';
import { getMyStudents } from '../../api/lecturers';

const LEVEL_COLORS = { 'HND 1': '#1565c0', HND2: '#2e7d32', 'B-TECH': '#e65100', 'M-TECH 1': '#c62828', 'M-TECH 2': '#6a1b9a' };
const LEVEL_BG = { 'HND 1': '#e3f2fd', HND2: '#e8f5e9', 'B-TECH': '#fff3e0', 'M-TECH 1': '#ffebee', 'M-TECH 2': '#f3e5f5' };

function groupByDeptAndLevel(students) {
  const groups = {};
  for (const s of students) {
    const dept = s.departmentName || 'Unassigned';
    if (!groups[dept]) groups[dept] = {};
    const level = s.currentLevel || 'Unknown';
    if (!groups[dept][level]) groups[dept][level] = [];
    groups[dept][level].push(s);
  }
  return groups;
}

export default function LecturerStudents() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedDepts, setExpandedDepts] = useState({});
  const [expandedLevels, setExpandedLevels] = useState({});

  useEffect(() => {
    getMyStudents()
      .then(res => {
        setStudents(res.data);
        const initDepts = {};
        for (const s of res.data) { initDepts[s.departmentName || 'Unassigned'] = true; }
        setExpandedDepts(initDepts);
      })
      .catch(err => setError(err.response?.data?.error || 'Failed to load students'))
      .finally(() => setLoading(false));
  }, []);

  const grouped = useMemo(() => groupByDeptAndLevel(students), [students]);
  const deptNames = Object.keys(grouped).sort();

  const toggleDept = (name) => setExpandedDepts(prev => ({ ...prev, [name]: !prev[name] }));
  const toggleLevel = (key) => setExpandedLevels(prev => ({ ...prev, [key]: !prev[key] }));

  if (loading) return <Box textAlign="center" py={4}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;

  if (students.length === 0) {
    return (
      <Box>
        <Typography variant="h5" gutterBottom fontWeight="bold">My Students</Typography>
        <Alert severity="info">No students enrolled in your courses yet.</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom fontWeight="bold">My Students</Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        {students.length} enrollment{students.length !== 1 ? 's' : ''} across your courses
      </Typography>

      {deptNames.map((deptName) => {
        const levels = grouped[deptName];
        const levelKeys = Object.keys(levels).sort((a, b) => {
          const levelOrder = { 'HND 1': 1, HND2: 2, 'B-TECH': 3, 'M-TECH 1': 4, 'M-TECH 2': 5 };
          return (levelOrder[a] || 0) - (levelOrder[b] || 0);
        });
        const totalInDept = levelKeys.reduce((sum, lv) => sum + levels[lv].length, 0);
        const deptOpen = expandedDepts[deptName] !== false;

        return (
          <Box key={deptName} sx={{ mb: 3 }}>
            <Paper
              elevation={0}
              sx={{
                p: 2,
                bgcolor: '#e3f2fd',
                borderLeft: '4px solid #1565c0',
                cursor: 'pointer',
                '&:hover': { bgcolor: '#bbdefb' },
              }}
              onClick={() => toggleDept(deptName)}
            >
              <Box display="flex" alignItems="center" justifyContent="space-between">
                <Box display="flex" alignItems="center" gap={1.5}>
                  <Avatar sx={{ bgcolor: '#1565c0', width: 36, height: 36 }}>
                    <BusinessIcon sx={{ fontSize: 20 }} />
                  </Avatar>
                  <Box>
                    <Typography variant="h6" fontWeight="bold">{deptName}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {totalInDept} student{totalInDept !== 1 ? 's' : ''} across {levelKeys.length} level{levelKeys.length !== 1 ? 's' : ''}
                    </Typography>
                  </Box>
                </Box>
                {deptOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
              </Box>
            </Paper>

            <Collapse in={deptOpen} timeout="auto">
              <Box sx={{ pl: 2 }}>
                {levelKeys.map((level) => {
                  const levelStudents = levels[level];
                  const levelKey = `${deptName}-${level}`;
                  const levelOpen = expandedLevels[levelKey] !== false;

                  return (
                    <Box key={levelKey} sx={{ mt: 2 }}>
                      <Paper
                        elevation={0}
                        sx={{
                          py: 1.5,
                          px: 2,
                          bgcolor: LEVEL_BG[level] || '#f5f5f5',
                          borderLeft: `4px solid ${LEVEL_COLORS[level] || '#9e9e9e'}`,
                          cursor: 'pointer',
                          '&:hover': { opacity: 0.9 },
                        }}
                        onClick={() => toggleLevel(levelKey)}
                      >
                        <Box display="flex" alignItems="center" justifyContent="space-between">
                          <Box display="flex" alignItems="center" gap={1}>
                            <SchoolIcon sx={{ color: LEVEL_COLORS[level] || '#9e9e9e', fontSize: 22 }} />
                            <Typography variant="subtitle1" fontWeight="bold">
                              {level}
                            </Typography>
                            <Chip
                              label={`${levelStudents.length} student${levelStudents.length !== 1 ? 's' : ''}`}
                              size="small"
                              sx={{ bgcolor: LEVEL_COLORS[level] || '#9e9e9e', color: '#fff' }}
                            />
                          </Box>
                          {levelOpen ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                        </Box>
                      </Paper>

                      <Collapse in={levelOpen} timeout="auto">
                        <TableContainer sx={{ mt: 1 }}>
                          <Table size="small">
                            <TableHead>
                              <TableRow sx={{ bgcolor: '#fafafa' }}>
                                <TableCell sx={{ fontWeight: 600 }}>Student ID</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Name</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Email</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Program</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Course</TableCell>
                                <TableCell sx={{ fontWeight: 600 }}>Semester</TableCell>
                              </TableRow>
                            </TableHead>
                            <TableBody>
                              {levelStudents.map((s, i) => (
                                <TableRow key={`${s.studentId}-${s.courseId}-${i}`} hover>
                                  <TableCell>{s.studentNumber}</TableCell>
                                  <TableCell>{s.fullName}</TableCell>
                                  <TableCell>{s.email}</TableCell>
                                  <TableCell>{s.programName || '-'}</TableCell>
                                  <TableCell>{s.courseCode} - {s.courseTitle}</TableCell>
                                  <TableCell>{s.semester} {s.academicYear}</TableCell>
                                </TableRow>
                              ))}
                            </TableBody>
                          </Table>
                        </TableContainer>
                      </Collapse>
                    </Box>
                  );
                })}
              </Box>
            </Collapse>
          </Box>
        );
      })}
    </Box>
  );
}
