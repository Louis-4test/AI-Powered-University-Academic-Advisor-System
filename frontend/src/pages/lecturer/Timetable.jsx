import { useState, useEffect } from 'react';
import {
  Box, Typography, Paper, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, CircularProgress, Chip,
} from '@mui/material';
import { getMyLecturerProfile } from '../../api/lecturers';
import { getTimetableByLecturer } from '../../api/timetable';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
const TIME_SLOTS = [
  '08:00 - 09:30', '09:45 - 11:15', '11:30 - 13:00', '14:00 - 15:30', '15:45 - 17:15',
];

export default function LecturerTimetable() {
  const [timetable, setTimetable] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const prof = await getMyLecturerProfile();
        const res = await getTimetableByLecturer(prof.data.id);
        setTimetable(res.data);
      } catch (err) { console.error(err); }
      finally { setLoading(false); }
    };
    fetch();
  }, []);

  const getEntriesForDayAndSlot = (day, slotLabel) => {
    const [startStr] = slotLabel.split(' - ');
    return timetable.filter(
      (e) => e.dayOfWeek === day && e.startTime === startStr
    );
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>My Timetable</Typography>
      {timetable.length === 0 ? (
        <Typography color="text.secondary">No timetable entries assigned to you.</Typography>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 'bold', minWidth: 120 }}>Time</TableCell>
                {DAYS.map((day) => (
                  <TableCell key={day} sx={{ fontWeight: 'bold' }} align="center">
                    {day.charAt(0) + day.slice(1).toLowerCase()}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {TIME_SLOTS.map((slot) => (
                <TableRow key={slot}>
                  <TableCell sx={{ whiteSpace: 'nowrap', color: 'text.secondary' }}>{slot}</TableCell>
                  {DAYS.map((day) => {
                    const entries = getEntriesForDayAndSlot(day, slot);
                    return (
                      <TableCell key={day} align="center" sx={{ p: 1 }}>
                        {entries.map((e) => (
                          <Box key={e.id} mb={0.5}>
                            <Chip
                              label={`${e.courseCode}`}
                              size="small"
                              color="secondary"
                              sx={{ fontWeight: 'bold', mb: 0.25 }}
                            />
                            <Typography variant="caption" display="block">
                              {e.courseTitle}
                            </Typography>
                            {e.room && (
                              <Typography variant="caption" color="text.secondary">
                                {e.room}
                              </Typography>
                            )}
                          </Box>
                        ))}
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
