import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { AuthProvider, useAuth } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Layout from './components/Layout';
import theme from './theme';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import StudentDashboard from './pages/student/Dashboard';
import StudentCourses from './pages/student/Courses';
import StudentResults from './pages/student/Results';
import LecturerDashboard from './pages/lecturer/Dashboard';
import LecturerCourses from './pages/lecturer/Courses';
import LecturerTimetable from './pages/lecturer/Timetable';
import LecturerExamGenerator from './pages/lecturer/ExamGenerator';
import LecturerResearchAssistant from './pages/lecturer/ResearchAssistant';
import LecturerStudents from './pages/lecturer/Students';
import AdminDashboard from './pages/admin/Dashboard';
import AdminStudents from './pages/admin/Students';
import AdminCourses from './pages/admin/Courses';
import AdminTimetable from './pages/admin/Timetable';
import AdminLecturers from './pages/admin/Lecturers';
import AdminDepartments from './pages/admin/Departments';
import StudentTimetable from './pages/student/Timetable';
import AiOverview from './pages/ai/AiOverview';
import AiChatbot from './pages/ai/Chatbot';
import AiRiskPrediction from './pages/ai/RiskPrediction';
import AiCareerRecommendation from './pages/ai/CareerRecommendation';
import AiResearchAssistant from './pages/ai/ResearchAssistant';
import AiExamGenerator from './pages/ai/ExamGenerator';

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'ADMIN') return <Navigate to="/admin" replace />;
  if (user.role === 'STUDENT') return <Navigate to="/student" replace />;
  if (user.role === 'LECTURER') return <Navigate to="/lecturer" replace />;
  return <Navigate to="/login" replace />;
}

function App() {
  return (
    <ThemeProvider theme={theme}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/" element={<HomeRedirect />} />

            <Route element={<PrivateRoute roles={['STUDENT']}><Layout /></PrivateRoute>}>
              <Route path="/student" element={<StudentDashboard />} />
              <Route path="/student/courses" element={<StudentCourses />} />
              <Route path="/student/timetable" element={<StudentTimetable />} />
              <Route path="/student/results" element={<StudentResults />} />
            </Route>

            <Route element={<PrivateRoute roles={['LECTURER']}><Layout /></PrivateRoute>}>
              <Route path="/lecturer" element={<LecturerDashboard />} />
              <Route path="/lecturer/courses" element={<LecturerCourses />} />
              <Route path="/lecturer/timetable" element={<LecturerTimetable />} />
              <Route path="/lecturer/exam-generator" element={<LecturerExamGenerator />} />
              <Route path="/lecturer/research-assistant" element={<LecturerResearchAssistant />} />
              <Route path="/lecturer/students" element={<LecturerStudents />} />
            </Route>

            <Route element={<PrivateRoute roles={['ADMIN']}><Layout /></PrivateRoute>}>
              <Route path="/admin" element={<AdminDashboard />} />
              <Route path="/admin/students" element={<AdminStudents />} />
              <Route path="/admin/courses" element={<AdminCourses />} />
              <Route path="/admin/timetable" element={<AdminTimetable />} />
              <Route path="/admin/lecturers" element={<AdminLecturers />} />
              <Route path="/admin/departments" element={<AdminDepartments />} />
            </Route>

            <Route element={<PrivateRoute roles={['ADMIN', 'STUDENT', 'LECTURER']}><Layout /></PrivateRoute>}>
              <Route path="/ai" element={<AiOverview />} />
              <Route path="/ai/chatbot" element={<AiChatbot />} />
              <Route path="/ai/risk-prediction" element={<AiRiskPrediction />} />
              <Route path="/ai/career" element={<AiCareerRecommendation />} />
              <Route path="/ai/research-assistant" element={<AiResearchAssistant />} />
              <Route path="/ai/exam-generator" element={<AiExamGenerator />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;
