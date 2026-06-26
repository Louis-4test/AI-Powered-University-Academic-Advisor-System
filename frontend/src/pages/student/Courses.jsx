import { useState, useEffect } from 'react';
import {
  Grid, Card, CardContent, Typography, CircularProgress, Chip, Box,
} from '@mui/material';
import { getMyProfile } from '../../api/students';
import { getCoursesByStudent } from '../../api/courses';
import { getEnrollmentsByStudent } from '../../api/enrollments';

export default function StudentCourses() {
  const [courses, setCourses] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const prof = await getMyProfile();
        const sid = prof.data.id;
        const [coursesRes, enrollRes] = await Promise.all([
          getCoursesByStudent(sid),
          getEnrollmentsByStudent(sid),
        ]);
        setCourses(coursesRes.data);
        setEnrollments(enrollRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>My Courses</Typography>
      <Grid container spacing={2}>
        {courses.map((course) => {
          const enrollment = enrollments.find((e) => e.courseCode === course.courseCode);
          return (
            <Grid item xs={12} sm={6} md={4} key={course.id}>
              <Card>
                <CardContent>
                  <Typography variant="h6" fontWeight="bold">{course.title}</Typography>
                  <Typography color="text.secondary" gutterBottom>{course.courseCode}</Typography>
                  <Typography variant="body2">{course.description}</Typography>
                  <Box mt={1} display="flex" gap={1} flexWrap="wrap">
                    <Chip label={`${course.creditHours} Credits`} size="small" />
                    <Chip label={`Level ${course.level}`} size="small" variant="outlined" />
                    {enrollment && (
                      <Chip
                        label={enrollment.status}
                        size="small"
                        color={enrollment.status === 'ENROLLED' ? 'info' : enrollment.status === 'COMPLETED' ? 'success' : 'default'}
                      />
                    )}
                  </Box>
                  {course.lecturerName && (
                    <Typography variant="caption" display="block" mt={1}>
                      Lecturer: {course.lecturerName}
                    </Typography>
                  )}
                </CardContent>
              </Card>
            </Grid>
          );
        })}
        {courses.length === 0 && (
          <Grid item xs={12}>
            <Typography color="text.secondary">No courses enrolled.</Typography>
          </Grid>
        )}
      </Grid>
    </Box>
  );
}
