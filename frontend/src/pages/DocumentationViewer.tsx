import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSessionStore } from '../store/sessionStore';
import { documentationService } from '../services/documentationService';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import {
  Container,
  Paper,
  Typography,
  Button,
  Box,
  LinearProgress,
  Alert,
  IconButton,
} from '@mui/material';
import {
  Download,
  Refresh,
  ArrowBack,
} from '@mui/icons-material';

const DocumentationViewer = () => {
  const navigate = useNavigate();
  const { sessionId } = useSessionStore();

  const [documentation, setDocumentation] = useState<string>('');
  const [generating, setGenerating] = useState<boolean>(false);
  const [progress, setProgress] = useState<number>(0);
  const [status, setStatus] = useState<string>('');
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (sessionId) {
      generateDocumentation();
    }
  }, [sessionId]);

  const generateDocumentation = async () => {
    if (!sessionId) {
      setError('No session ID found');
      return;
    }

    setGenerating(true);
    setError('');
    setProgress(0);

    try {
      const progressData = await documentationService.generateWithProgress(sessionId);
      setStatus(progressData.status);
      setProgress(progressData.progress);

      if (progressData.documentation) {
        setDocumentation(progressData.documentation);
        setGenerating(false);
      } else if (progressData.error) {
        setError(progressData.error);
        setGenerating(false);
      } else {
        // Poll for completion
        const pollInterval = setInterval(async () => {
          try {
            const updated = await documentationService.generateWithProgress(sessionId);
            setStatus(updated.status);
            setProgress(updated.progress);

            if (updated.documentation) {
              setDocumentation(updated.documentation);
              setGenerating(false);
              clearInterval(pollInterval);
            } else if (updated.error) {
              setError(updated.error);
              setGenerating(false);
              clearInterval(pollInterval);
            }
          } catch (err: any) {
            setError(err.message || 'Failed to generate documentation');
            setGenerating(false);
            clearInterval(pollInterval);
          }
        }, 2000);

        setTimeout(() => {
          clearInterval(pollInterval);
          if (generating) {
            setError('Documentation generation timed out');
            setGenerating(false);
          }
        }, 300000);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate documentation');
      setGenerating(false);
    }
  };

  const handleDownload = async () => {
    if (!sessionId) return;

    try {
      const blob = await documentationService.download(sessionId, 'markdown');
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'README.md';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      setError('Failed to download documentation');
    }
  };

  const handleBack = () => {
    navigate('/dashboard');
  };

  if (!sessionId) {
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 4 }}>
          No session found. Please upload a repository first.
        </Alert>
        <Button onClick={handleBack} sx={{ mt: 2 }}>
          Back to Dashboard
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton onClick={handleBack}>
              <ArrowBack />
            </IconButton>
            <Typography variant="h4">
              Generated Documentation
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={generateDocumentation}
              disabled={generating}
            >
              Regenerate
            </Button>
            <Button
              variant="contained"
              startIcon={<Download />}
              onClick={handleDownload}
              disabled={!documentation || generating}
            >
              Download README.md
            </Button>
          </Box>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        {generating && (
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="body1" gutterBottom>
              {status || 'Generating documentation...'}
            </Typography>
            <LinearProgress variant="determinate" value={progress} sx={{ mt: 2 }} />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {progress}% complete
            </Typography>
          </Paper>
        )}

        {documentation && !generating && (
          <Paper sx={{ p: 4 }}>
            <Box sx={{ 
              '& h1': { mb: 2 },
              '& h2': { mt: 4, mb: 2 },
              '& h3': { mt: 3, mb: 1 },
              '& code': { bgcolor: 'background.default', p: 0.5, borderRadius: 1 },
              '& pre': { bgcolor: 'background.default', p: 2, borderRadius: 1, overflow: 'auto' },
            }}>
              <ReactMarkdown remarkPlugins={[remarkGfm]}>
                {documentation}
              </ReactMarkdown>
            </Box>
          </Paper>
        )}

        {!documentation && !generating && !error && (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="body1" color="text.secondary">
              Click "Regenerate" to generate documentation
            </Typography>
          </Paper>
        )}
      </Box>
    </Container>
  );
};

export default DocumentationViewer;

