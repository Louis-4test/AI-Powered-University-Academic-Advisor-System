import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  AppBar, Box, CssBaseline, Drawer, IconButton, List, ListItem,
  ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography,
  Button, Divider,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SchoolIcon from '@mui/icons-material/School';
import PeopleIcon from '@mui/icons-material/People';
import BookIcon from '@mui/icons-material/Book';
import DashboardIcon from '@mui/icons-material/Dashboard';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import AssignmentIcon from '@mui/icons-material/Assignment';
import PsychologyIcon from '@mui/icons-material/Psychology';
import WorkIcon from '@mui/icons-material/Work';
import DescriptionIcon from '@mui/icons-material/Description';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import LogoutIcon from '@mui/icons-material/Logout';
import { useAuth } from '../context/AuthContext';

const drawerWidth = 260;

const menuConfig = {
  ADMIN: [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/admin' },
    { text: 'Students', icon: <PeopleIcon />, path: '/admin/students' },
    { text: 'Courses', icon: <BookIcon />, path: '/admin/courses' },
    { text: 'Timetable', icon: <CalendarMonthIcon />, path: '/admin/timetable' },
    { text: 'Lecturers', icon: <SchoolIcon />, path: '/admin/lecturers' },
    { text: 'Departments', icon: <DescriptionIcon />, path: '/admin/departments' },
    { text: 'AI Tools', icon: <SmartToyIcon />, path: '/ai' },
  ],
  STUDENT: [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/student' },
    { text: 'My Courses', icon: <BookIcon />, path: '/student/courses' },
    { text: 'Timetable', icon: <CalendarMonthIcon />, path: '/student/timetable' },
    { text: 'Results', icon: <AssignmentIcon />, path: '/student/results' },
    { text: 'AI Advisor', icon: <SmartToyIcon />, path: '/ai' },
  ],
  LECTURER: [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/lecturer' },
    { text: 'My Courses', icon: <BookIcon />, path: '/lecturer/courses' },
    { text: 'Students', icon: <PeopleIcon />, path: '/lecturer/students' },
    { text: 'Timetable', icon: <CalendarMonthIcon />, path: '/lecturer/timetable' },
    { text: 'Exam Generator', icon: <AssignmentIcon />, path: '/lecturer/exam-generator' },
    { text: 'Research Assistant', icon: <PsychologyIcon />, path: '/lecturer/research-assistant' },
    { text: 'AI Tools', icon: <SmartToyIcon />, path: '/ai' },
  ],
};

export default function Layout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = menuConfig[user?.role] || [];

  const drawer = (
    <Box>
      <Toolbar>
        <Typography variant="h6" noWrap fontWeight="bold" color="primary">
          Academic Advisor
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => { navigate(item.path); setMobileOpen(false); }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar position="fixed" sx={{ width: { sm: `calc(100% - ${drawerWidth}px)` }, ml: { sm: `${drawerWidth}px` } }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" sx={{ mr: 2, display: { sm: 'none' } }} onClick={() => setMobileOpen(!mobileOpen)}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>
            {user?.fullName} ({user?.role})
          </Typography>
          <Button color="inherit" startIcon={<LogoutIcon />} onClick={() => { logout(); navigate('/login'); }}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, p: 3, mt: 8 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
