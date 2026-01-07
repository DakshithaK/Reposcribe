import { useState, useEffect } from 'react';
import api from '../services/api';

const HealthCheck = () => {
  const [message, setMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await api.get('/health');
        setMessage(response.data);
        setLoading(false);
      } catch (error) {
        setMessage('Failed to connect to backend');
        setLoading(false);
      }
    };

    checkHealth();
  }, []);

  if (loading) {
    return <div>Checking connection...</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h2>Backend Connection Test</h2>
      <p>{message}</p>
    </div>
  );
};

export default HealthCheck;

