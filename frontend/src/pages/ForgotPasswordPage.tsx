import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../api/auth.service';

const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [codeSent, setCodeSent] = useState(false);
  const navigate = useNavigate();

  const handleSendCode = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.forgotPassword(email);
      setCodeSent(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка при отправке кода');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();

    if (code.length !== 6) {
      setError('Код должен содержать 6 цифр');
      return;
    }

    setError('');
    setLoading(true);

    try {
      await authService.verifyResetCode(email, code);
      navigate('/reset-password', { state: { email, code } });
    } catch (err: any) {
      setError(err.response?.data?.message || 'Неверный код');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '100px auto', padding: '20px' }}>
      <h2>{codeSent ? 'Введите код' : 'Забыли пароль?'}</h2>
      <p style={{ marginBottom: '20px', color: '#666' }}>
        {codeSent
          ? `Введите код из письма, отправленного на ${email}`
          : 'Введите email для получения кода восстановления'}
      </p>

      <form onSubmit={codeSent ? handleVerifyCode : handleSendCode}>
        <div style={{ marginBottom: '15px' }}>
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={codeSent}
            required
            style={{
              width: '100%',
              padding: '8px',
              marginTop: '5px',
              backgroundColor: codeSent ? '#f0f0f0' : 'white'
            }}
          />
        </div>

        {codeSent && (
          <div style={{ marginBottom: '15px' }}>
            <label>Код подтверждения:</label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
              maxLength={6}
              required
              placeholder="000000"
              style={{ width: '100%', padding: '8px', marginTop: '5px' }}
            />
          </div>
        )}

        {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}

        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: '10px',
            backgroundColor: loading ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            cursor: loading ? 'not-allowed' : 'pointer',
            marginBottom: '10px'
          }}
        >
          {loading ? 'Загрузка...' : (codeSent ? 'Продолжить' : 'Отправить код')}
        </button>

        {codeSent && (
          <button
            type="button"
            onClick={handleSendCode}
            disabled={loading}
            style={{
              width: '100%',
              padding: '10px',
              backgroundColor: 'transparent',
              color: '#007bff',
              border: '1px solid #007bff',
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
          >
            Отправить код повторно
          </button>
        )}

        <button
          type="button"
          onClick={() => navigate('/login')}
          style={{
            width: '100%',
            padding: '10px',
            marginTop: '10px',
            backgroundColor: 'transparent',
            color: '#666',
            border: 'none',
            cursor: 'pointer',
            textDecoration: 'underline'
          }}
        >
          Вернуться к входу
        </button>
      </form>
    </div>
  );
};

export default ForgotPasswordPage;
