import { useState } from 'react';
import {
  Box,
  Button,
  TextField,
  Typography,
  Alert,
  LinearProgress,
  Paper,
  FormControlLabel,
  Checkbox,
  Link,
} from '@mui/material';
import { GitHub, Code } from '@mui/icons-material';
import { gitService } from '../services/gitService';

interface GitCloneProps {
  onCloneSuccess: (sessionId: string) => void;
}

const GitClone = ({ onCloneSuccess }: GitCloneProps) => {
  const [repositoryUrl, setRepositoryUrl] = useState('');
  const [isPrivate, setIsPrivate] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [cloning, setCloning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const validateGitUrl = (url: string): boolean => {
    if (!url.trim()) return false;
    
    // Basic validation for common Git URL formats
    const patterns = [
      /^https?:\/\/.+/,
      /^git@.+/,
      /^.+\.git$/,
    ];
    
    return patterns.some(pattern => pattern.test(url));
  };

  const handleClone = async () => {
    // Reset states
    setError(null);
    setSuccess(null);
    setProgress(0);

    // Validate URL
    if (!repositoryUrl.trim()) {
      setError('Please enter a repository URL');
      return;
    }

    if (!validateGitUrl(repositoryUrl)) {
      setError('Please enter a valid Git repository URL');
      return;
    }

    // Validate credentials if private repo
    if (isPrivate && (!username.trim() || !password.trim())) {
      setError('Username and password/token are required for private repositories');
      return;
    }

    setCloning(true);

    try {
      // Simulate progress (Git clone doesn't have built-in progress events)
      const progressInterval = setInterval(() => {
        setProgress((prev) => {
          if (prev >= 90) {
            clearInterval(progressInterval);
            return 90;
          }
          return prev + 10;
        });
      }, 500);

      const response = await gitService.cloneRepository(
        repositoryUrl,
        isPrivate ? username : undefined,
        isPrivate ? password : undefined
      );

      clearInterval(progressInterval);
      setProgress(100);

      if (response.success && response.sessionId) {
        setSuccess(response.message || 'Repository cloned successfully!');
        setTimeout(() => {
          onCloneSuccess(response.sessionId!);
        }, 1000);
      } else {
        setError(response.message || 'Clone failed');
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message || 
        'Failed to clone repository. Please check the URL and try again.'
      );
      setProgress(0);
    } finally {
      setCloning(false);
    }
  };

  const exampleUrls = [
    'https://github.com/spring-projects/spring-boot.git',
    'https://github.com/facebook/react.git',
    'git@github.com:username/repo.git',
  ];

  return (
    <Paper elevation={3} sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <GitHub />
        <Typography variant="h6">
          Clone from Git Repository
        </Typography>
      </Box>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Enter a Git repository URL to clone. Supports public and private repositories.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Box sx={{ mb: 2 }}>
        <TextField
          fullWidth
          label="Repository URL"
          placeholder="https://github.com/username/repository.git"
          value={repositoryUrl}
          onChange={(e) => setRepositoryUrl(e.target.value)}
          disabled={cloning}
          sx={{ mb: 2 }}
          helperText="Supports HTTPS, SSH, and GitHub URLs"
        />

        <FormControlLabel
          control={
            <Checkbox
              checked={isPrivate}
              onChange={(e) => setIsPrivate(e.target.checked)}
              disabled={cloning}
            />
          }
          label="Private repository (requires credentials)"
        />

        {isPrivate && (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              label="Username"
              placeholder="GitHub username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={cloning}
              sx={{ mb: 2 }}
            />
            <TextField
              fullWidth
              label="Password / Personal Access Token"
              type="password"
              placeholder="Personal access token (not password)"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={cloning}
              helperText={
                <Link
                  href="https://github.com/settings/tokens"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Generate GitHub token
                </Link>
              }
            />
          </Box>
        )}

        {cloning && (
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Cloning repository... {progress}%
            </Typography>
            <LinearProgress variant="determinate" value={progress} />
          </Box>
        )}
      </Box>

      <Button
        variant="contained"
        onClick={handleClone}
        disabled={!repositoryUrl.trim() || cloning}
        fullWidth
        startIcon={<Code />}
      >
        {cloning ? 'Cloning...' : 'Clone Repository'}
      </Button>

      <Box sx={{ mt: 3 }}>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Example URLs:
        </Typography>
        <Box component="ul" sx={{ pl: 2, m: 0 }}>
          {exampleUrls.map((url, index) => (
            <li key={index}>
              <Typography
                variant="caption"
                component="code"
                sx={{
                  cursor: 'pointer',
                  color: 'primary.main',
                  '&:hover': { textDecoration: 'underline' },
                }}
                onClick={() => !cloning && setRepositoryUrl(url)}
              >
                {url}
              </Typography>
            </li>
          ))}
        </Box>
      </Box>
    </Paper>
  );
};

export default GitClone;

