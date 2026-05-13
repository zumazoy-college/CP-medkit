import { useState, useEffect } from 'react';
import templateService from '../services/templateService';
import { Template } from '../types';

export const useTemplates = () => {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await templateService.getMyTemplates();
      setTemplates(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to fetch templates');
      console.error('Error fetching templates:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  return { templates, loading, error, refetch: fetchTemplates };
};
