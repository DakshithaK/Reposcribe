import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useSessionStore } from '../store/sessionStore';
import { useNavigate } from 'react-router-dom';
import FileUpload from '../components/FileUpload';
import GitClone from '../components/GitClone';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Tabs,
  Tab,
} from '@mui/material';
import { CloudUpload, Code } from '@mui/icons-material';

const Dashboard = () => {
  const { username, logout } = useAuthStore();
  const { sessionId, setSession } = useSessionStore();
  const navigate = useNavigate();
  const [tabValue, setTabValue] = useState(0);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleUploadSuccess = (newSessionId: string) => {
    setSession(newSessionId, 'file');
    navigate('/documentation');
  };

  const handleCloneSuccess = (newSessionId: string) => {
    setSession(newSessionId, 'git');
    navigate('/documentation');
  };

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4">
            Welcome, {username}!
          </Typography>
          <Button variant="outlined" color="error" onClick={handleLogout}>
            Logout
          </Button>
        </Box>

        <Paper elevation={2} sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Upload Your Repository
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Choose how you want to provide your code repository for documentation generation.
          </Typography>

          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)} sx={{ mb: 3 }}>
            <Tab icon={<CloudUpload />} label="Upload ZIP File" iconPosition="start" />
            <Tab icon={<Code />} label="Clone from Git" iconPosition="start" />
          </Tabs>

          {tabValue === 0 && (
            <FileUpload onUploadSuccess={handleUploadSuccess} />
          )}

          {tabValue === 1 && (
            <GitClone onCloneSuccess={handleCloneSuccess} />
          )}

          {sessionId && (
            <Box sx={{ mt: 3, p: 2, bgcolor: 'success.light', borderRadius: 1 }}>
              <Typography variant="body2" color="success.dark">
                ✓ Repository {tabValue === 0 ? 'uploaded' : 'cloned'} successfully! Session ID: {sessionId}
              </Typography>
            </Box>
          )}
        </Paper>
      </Box>
    </Container>
  );
};

export default Dashboard;
