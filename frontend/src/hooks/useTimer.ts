import { useState, useEffect } from 'react';

export const useTimer = (targetDate: Date | null) => {
  const [timeRemaining, setTimeRemaining] = useState<string>('');

  useEffect(() => {
    if (!targetDate || isNaN(targetDate.getTime())) {
      setTimeRemaining('');
      return;
    }

    const updateTimer = () => {
      const now = new Date();
      const diff = targetDate.getTime() - now.getTime();

      if (diff <= 0) {
        setTimeRemaining('');
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

      if (hours > 0) {
        setTimeRemaining(`${hours} ч ${minutes} мин`);
      } else {
        setTimeRemaining(`${minutes} мин`);
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 60000);

    return () => clearInterval(interval);
  }, [targetDate]);

  return timeRemaining;
};
