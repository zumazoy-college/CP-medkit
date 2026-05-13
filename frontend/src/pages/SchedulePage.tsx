import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import scheduleService, { Schedule, AppointmentSlot, CreateScheduleData, CreateScheduleExceptionData, ScheduleException } from '../api/schedule.service';
import './SchedulePage.css';

const SchedulePage: React.FC = () => {
  const navigate = useNavigate();
  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [showBlockModal, setShowBlockModal] = useState(false);
  const [blockModalMode, setBlockModalMode] = useState<'block' | 'unblock'>('block');
  const [viewMode, setViewMode] = useState<'all' | 'free' | 'occupied'>('all');
  const [calendarView, setCalendarView] = useState<'day' | 'week'>('week');
  const [scheduleMode, setScheduleMode] = useState<'all' | 'individual'>('all'); // Режим настройки расписания

  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [slots, setSlots] = useState<AppointmentSlot[]>([]);
  const [blockedDates, setBlockedDates] = useState<ScheduleException[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [currentDate, setCurrentDate] = useState(new Date());

  // Форма настройки расписания
  const [scheduleForm, setScheduleForm] = useState<{[key: number]: CreateScheduleData}>({});
  const [selectedDays, setSelectedDays] = useState<number[]>([1, 2, 3, 4, 5]); // Пн-Пт по умолчанию

  // Форма блокировки даты
  const [blockForm, setBlockForm] = useState<CreateScheduleExceptionData>({
    reason: '',
    startDate: '',
    endDate: '',
  });

  useEffect(() => {
    loadScheduleData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentDate, calendarView]);

  const loadScheduleData = async () => {
    setLoading(true);
    setError(null);
    try {
      const schedulesData = await scheduleService.getMySchedule();
      setSchedules(schedulesData);

      const { startDate, endDate } = getDateRange();
      const slotsData = await scheduleService.getMySlotsInRange(startDate, endDate);
      setSlots(slotsData);

      // Загружаем заблокированные даты текущего врача
      const exceptionsData = await scheduleService.getMyScheduleExceptionsByDateRange(
        startDate,
        endDate
      );
      setBlockedDates(exceptionsData);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки данных');
    } finally {
      setLoading(false);
    }
  };

  const handleSlotClick = (slot: AppointmentSlot) => {
    if (slot.patientId && slot.appointmentId) {
      navigate(`/doctor/patients/${slot.patientId}?appointmentId=${slot.appointmentId}&tab=examination`);
    }
  };

  const getDateRange = () => {
    const start = new Date(currentDate);
    let end = new Date(currentDate);

    if (calendarView === 'day') {
      end = new Date(start);
    } else {
      // Неделя: с понедельника по воскресенье
      const dayOfWeek = start.getDay();
      const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
      start.setDate(start.getDate() + diff);
      end = new Date(start);
      end.setDate(end.getDate() + 6);
    }

    return {
      startDate: start.toISOString().split('T')[0],
      endDate: end.toISOString().split('T')[0],
    };
  };

  const getWeekNumber = (date: Date) => {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const dayNum = d.getUTCDay() || 7;
    d.setUTCDate(d.getUTCDate() + 4 - dayNum);
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  };

  const getWeekDisplay = () => {
    if (calendarView === 'day') {
      return currentDate.toLocaleDateString('ru-RU', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    } else {
      const { startDate, endDate } = getDateRange();
      const start = new Date(startDate);
      const end = new Date(endDate);
      const weekNum = getWeekNumber(start);

      return `Неделя ${weekNum} (${start.getDate()} ${start.toLocaleDateString('ru-RU', { month: 'short' })} - ${end.getDate()} ${end.toLocaleDateString('ru-RU', { month: 'short', year: 'numeric' })})`;
    }
  };

  const handleSaveSchedule = async () => {
    setLoading(true);
    setError(null);
    try {
      // Validate date ranges
      const firstDaySchedule = scheduleForm[selectedDays[0]];
      if (!firstDaySchedule) {
        setError('Не удалось получить данные расписания');
        setLoading(false);
        return;
      }

      const effectiveFrom = firstDaySchedule.effectiveFrom || new Date().toISOString().split('T')[0];
      const effectiveTo = firstDaySchedule.effectiveTo;

      if (effectiveTo) {
        const startDate = new Date(effectiveFrom);
        const endDate = new Date(effectiveTo);

        if (endDate <= startDate) {
          setError('Дата конца действия расписания должна быть позже даты начала');
          setLoading(false);
          return;
        }
      }

      // Проверка конфликтов при изменении существующего расписания
      const hasExistingSchedules = selectedDays.some(day =>
        schedules.find(s => s.dayOfWeek === day)
      );

      if (hasExistingSchedules) {
        // Проверяем, есть ли занятые слоты в ближайшие 2 недели
        const today = new Date();
        const twoWeeksLater = new Date(today);
        twoWeeksLater.setDate(twoWeeksLater.getDate() + 14);

        const upcomingSlots = await scheduleService.getMySlotsInRange(
          today.toISOString().split('T')[0],
          twoWeeksLater.toISOString().split('T')[0]
        );

        const bookedSlots = upcomingSlots.filter(slot =>
          slot.status === 'booked' || slot.status === 'completed'
        );

        if (bookedSlots.length > 0) {
          const confirmMessage = `Внимание! У вас есть ${bookedSlots.length} записей на ближайшие 2 недели. При изменении расписания убедитесь, что новое время работы не конфликтует с существующими записями. Продолжить?`;

          if (!window.confirm(confirmMessage)) {
            setLoading(false);
            return;
          }
        }
      }

      // Подготовка данных для пакетного обновления
      const schedulesToUpdate = selectedDays.map(dayOfWeek => {
        const formData = scheduleForm[dayOfWeek];
        return {
          dayOfWeek,
          startTime: formData.startTime,
          endTime: formData.endTime,
          lunchStart: formData.lunchStart,
          lunchEnd: formData.lunchEnd,
          appointmentDuration: formData.appointmentDuration,
        };
      });

      // Используем новый endpoint для пакетного обновления
      await scheduleService.batchUpdateSchedules({
        effectiveFrom,
        effectiveTo,
        schedules: schedulesToUpdate,
      });

      setShowSettingsModal(false);
      await loadScheduleData();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка сохранения расписания');
    } finally {
      setLoading(false);
    }
  };

  const handleBlockDate = async () => {
    if (!blockForm.startDate || !blockForm.endDate) {
      setError('Заполните даты начала и окончания');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await scheduleService.createScheduleException(blockForm);
      setShowBlockModal(false);
      setBlockForm({ reason: '', startDate: '', endDate: '' });
      await loadScheduleData();
    } catch (err: any) {
      // Check if error is about existing appointments
      if (err.response?.data?.message?.includes('записей')) {
        const confirmBlock = window.confirm(
          err.response.data.message + '\n\nВы уверены, что хотите заблокировать эти даты? Все записи будут отменены.'
        );

        if (confirmBlock) {
          // User confirmed, try again with forceBlock flag
          try {
            await scheduleService.createScheduleException({
              ...blockForm,
              forceBlock: true
            });
            setShowBlockModal(false);
            setBlockForm({ reason: '', startDate: '', endDate: '' });
            await loadScheduleData();
          } catch (retryErr: any) {
            setError(retryErr.response?.data?.message || 'Ошибка блокировки даты');
          }
        }
      } else {
        setError(err.response?.data?.message || 'Ошибка блокировки даты');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUnblockDate = async () => {
    if (!blockForm.startDate || !blockForm.endDate) {
      setError('Заполните даты начала и окончания');
      return;
    }

    // Find exceptions in the selected date range
    const exceptionsToDelete = blockedDates.filter(exception => {
      const exStart = new Date(exception.startDate);
      const exEnd = new Date(exception.endDate);
      const selStart = new Date(blockForm.startDate);
      const selEnd = new Date(blockForm.endDate);

      // Check if exception overlaps with selected range
      return (exStart <= selEnd && exEnd >= selStart);
    });

    if (exceptionsToDelete.length === 0) {
      setError('В указанном диапазоне нет заблокированных дат');
      return;
    }

    const confirmUnblock = window.confirm(
      `Вы точно хотите разблокировать ${blockForm.startDate} - ${blockForm.endDate}?`
    );

    if (!confirmUnblock) {
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // Delete all overlapping exceptions
      for (const exception of exceptionsToDelete) {
        await scheduleService.deleteScheduleException(exception.idException);
      }

      setShowBlockModal(false);
      setBlockForm({ reason: '', startDate: '', endDate: '' });
      await loadScheduleData();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка разблокировки даты');
    } finally {
      setLoading(false);
    }
  };

  const handleDayToggle = (dayOfWeek: number) => {
    setSelectedDays(prev => {
      const isRemoving = prev.includes(dayOfWeek);

      if (isRemoving) {
        return prev.filter(d => d !== dayOfWeek);
      } else {
        // Adding a new day
        const newDays = [...prev, dayOfWeek];

        // Sort days in correct weekday order (Monday=1, Sunday=0 goes last)
        newDays.sort((a, b) => {
          const orderA = a === 0 ? 7 : a;
          const orderB = b === 0 ? 7 : b;
          return orderA - orderB;
        });

        // If in "all days" mode and there are existing days with schedules
        if (scheduleMode === 'all' && prev.length > 0 && scheduleForm[prev[0]]) {
          // Auto-apply the same schedule to the new day
          const firstDayData = scheduleForm[prev[0]];
          setScheduleForm(prevForm => ({
            ...prevForm,
            [dayOfWeek]: {
              dayOfWeek,
              startTime: firstDayData.startTime,
              endTime: firstDayData.endTime,
              lunchStart: firstDayData.lunchStart,
              lunchEnd: firstDayData.lunchEnd,
              appointmentDuration: firstDayData.appointmentDuration,
              effectiveFrom: firstDayData.effectiveFrom,
              effectiveTo: firstDayData.effectiveTo,
            }
          }));
        }

        return newDays;
      }
    });
  };

  const handleScheduleFormChange = (dayOfWeek: number, field: keyof CreateScheduleData, value: any) => {
    setScheduleForm(prev => ({
      ...prev,
      [dayOfWeek]: {
        ...prev[dayOfWeek],
        dayOfWeek,
        [field]: value,
      },
    }));
  };

  const initializeScheduleForm = () => {
    const form: {[key: number]: CreateScheduleData} = {};

    // If there are existing schedules, load days from them
    if (schedules.length > 0) {
      const existingDays = schedules.map(s => s.dayOfWeek);

      // Sort days in correct weekday order (Monday=1, Sunday=0 goes last)
      existingDays.sort((a, b) => {
        const orderA = a === 0 ? 7 : a;
        const orderB = b === 0 ? 7 : b;
        return orderA - orderB;
      });

      setSelectedDays(existingDays);

      // Check if all schedules are identical (same times, duration, lunch)
      const first = schedules[0];
      const allIdentical = schedules.every(s =>
        s.startTime === first.startTime &&
        s.endTime === first.endTime &&
        s.lunchStart === first.lunchStart &&
        s.lunchEnd === first.lunchEnd &&
        s.appointmentDuration === first.appointmentDuration
      );

      // Set schedule mode based on whether schedules are identical
      setScheduleMode(allIdentical ? 'all' : 'individual');

      // Initialize form data for each existing day
      existingDays.forEach(day => {
        const existing = schedules.find(s => s.dayOfWeek === day);
        if (existing) {
          form[day] = {
            dayOfWeek: existing.dayOfWeek,
            startTime: existing.startTime,
            endTime: existing.endTime,
            lunchStart: existing.lunchStart,
            lunchEnd: existing.lunchEnd,
            appointmentDuration: existing.appointmentDuration,
            effectiveFrom: existing.effectiveFrom,
            effectiveTo: existing.effectiveTo,
          };
        }
      });
    } else {
      // No existing schedules, use default selected days
      selectedDays.forEach(day => {
        form[day] = {
          dayOfWeek: day,
          startTime: '09:00',
          endTime: '17:00',
          lunchStart: '13:00',
          lunchEnd: '13:30',
          appointmentDuration: 30,
          effectiveFrom: new Date().toISOString().split('T')[0],
          effectiveTo: undefined,
        };
      });
    }

    setScheduleForm(form);
  };

  useEffect(() => {
    if (showSettingsModal) {
      initializeScheduleForm();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showSettingsModal, schedules]);

  // Синхронизация всех дней при переключении в режим "для всех дней"
  useEffect(() => {
    if (scheduleMode === 'all' && selectedDays.length > 0 && scheduleForm[selectedDays[0]]) {
      const firstDayData = scheduleForm[selectedDays[0]];

      // Check if synchronization is needed
      const needsSync = selectedDays.some(day => {
        if (day === selectedDays[0]) return false;
        const dayData = scheduleForm[day];
        if (!dayData) return true;
        return dayData.startTime !== firstDayData.startTime ||
               dayData.endTime !== firstDayData.endTime ||
               dayData.lunchStart !== firstDayData.lunchStart ||
               dayData.lunchEnd !== firstDayData.lunchEnd ||
               dayData.appointmentDuration !== firstDayData.appointmentDuration ||
               dayData.effectiveFrom !== firstDayData.effectiveFrom ||
               dayData.effectiveTo !== firstDayData.effectiveTo;
      });

      if (needsSync) {
        const updatedForm = { ...scheduleForm };

        selectedDays.forEach(day => {
          if (day !== selectedDays[0]) {
            updatedForm[day] = {
              ...updatedForm[day],
              startTime: firstDayData.startTime,
              endTime: firstDayData.endTime,
              lunchStart: firstDayData.lunchStart,
              lunchEnd: firstDayData.lunchEnd,
              appointmentDuration: firstDayData.appointmentDuration,
              effectiveFrom: firstDayData.effectiveFrom,
              effectiveTo: firstDayData.effectiveTo,
            };
          }
        });

        setScheduleForm(updatedForm);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scheduleMode]);

  const filteredSlots = slots.filter((slot) => {
    if (viewMode === 'free') return slot.status === 'free';
    if (viewMode === 'occupied') return slot.status !== 'free';
    return true;
  });

  const groupSlotsByDate = () => {
    const grouped: { [date: string]: AppointmentSlot[] } = {};
    filteredSlots.forEach(slot => {
      if (!grouped[slot.slotDate]) {
        grouped[slot.slotDate] = [];
      }
      grouped[slot.slotDate].push(slot);
    });
    return grouped;
  };

  const slotsByDate = groupSlotsByDate();

  const navigateDate = (direction: 'prev' | 'next') => {
    const newDate = new Date(currentDate);
    if (calendarView === 'day') {
      newDate.setDate(newDate.getDate() + (direction === 'next' ? 1 : -1));
    } else {
      newDate.setDate(newDate.getDate() + (direction === 'next' ? 7 : -7));
    }
    setCurrentDate(newDate);
  };

  const dayNames = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
  const dayNamesSchedule = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  return (
    <Layout>
      <div className="schedule-page">
        <div className="page-header">
          <h1>Расписание</h1>
          <div className="header-actions">
            <button className="btn btn-secondary" onClick={() => setShowSettingsModal(true)}>
              ⚙️ Настроить расписание
            </button>
            <button className="btn btn-secondary" onClick={() => {
              setBlockModalMode('block');
              setShowBlockModal(true);
            }}>
              🚫 Заблокировать дату
            </button>
            <button className="btn btn-secondary" onClick={() => {
              setBlockModalMode('unblock');
              setShowBlockModal(true);
            }}>
              ✅ Разблокировать дату
            </button>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="schedule-controls">
          <div className="schedule-filters">
            <button
              className={`filter-btn ${viewMode === 'all' ? 'active' : ''}`}
              onClick={() => setViewMode('all')}
            >
              Все слоты
            </button>
            <button
              className={`filter-btn ${viewMode === 'free' ? 'active' : ''}`}
              onClick={() => setViewMode('free')}
            >
              Свободные
            </button>
            <button
              className={`filter-btn ${viewMode === 'occupied' ? 'active' : ''}`}
              onClick={() => setViewMode('occupied')}
            >
              Занятые
            </button>
          </div>

          <div className="view-toggle">
            <button
              className={`view-btn ${calendarView === 'day' ? 'active' : ''}`}
              onClick={() => setCalendarView('day')}
            >
              День
            </button>
            <button
              className={`view-btn ${calendarView === 'week' ? 'active' : ''}`}
              onClick={() => setCalendarView('week')}
            >
              Неделя
            </button>
          </div>
        </div>

        <div className="schedule-calendar">
          <div className="calendar-header">
            <button onClick={() => navigateDate('prev')}>&larr;</button>
            <h2>{getWeekDisplay()}</h2>
            <button onClick={() => navigateDate('next')}>&rarr;</button>
          </div>

          {loading ? (
            <div className="loading">Загрузка...</div>
          ) : (
            <div className="calendar-grid">
              {Object.keys(slotsByDate).sort().map(date => {
                const isBlocked = blockedDates.some(
                  exception => date >= exception.startDate && date <= exception.endDate
                );
                const blockReason = blockedDates.find(
                  exception => date >= exception.startDate && date <= exception.endDate
                )?.reason;

                return (
                  <div key={date} className={`day-column ${isBlocked ? 'blocked' : ''}`}>
                    <div className="day-header">
                      <div className="day-name">{dayNames[new Date(date).getDay()]}</div>
                      <div className="day-date">{new Date(date).getDate()}</div>
                      {isBlocked && <div className="blocked-indicator">🚫</div>}
                    </div>
                    {isBlocked ? (
                      <div className="blocked-message">
                        <p>Заблокировано</p>
                        {blockReason && <p className="block-reason">{blockReason}</p>}
                      </div>
                    ) : (
                      <div className="slots-list">
                        {slotsByDate[date].map((slot) => (
                          <div
                            key={slot.idSlot}
                            className={`slot-item ${slot.status}`}
                            onClick={() => slot.status !== 'free' && slot.patientId && handleSlotClick(slot)}
                            style={{ cursor: slot.status !== 'free' && slot.patientId ? 'pointer' : 'default' }}
                          >
                            <div className="slot-time">{slot.startTime}</div>
                            <div className="slot-info">
                              {slot.status === 'free' ? (
                                <span className="slot-free">Свободно</span>
                              ) : slot.status === 'cancelled' ? (
                                <span className="slot-cancelled">Отменено</span>
                              ) : (
                                <span className="slot-patient">{slot.patientName || 'Занято'}</span>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {showSettingsModal && (
          <div className="modal-overlay" onClick={() => setShowSettingsModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Настройка рабочего расписания</h2>
                <button className="modal-close" onClick={() => setShowSettingsModal(false)}>
                  ✕
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Рабочие дни</label>
                  <div className="days-checkboxes">
                    {dayNamesSchedule.map((day, index) => {
                      const dayOfWeek = index === 6 ? 0 : index + 1;
                      return (
                        <label key={day} className="checkbox-label">
                          <input
                            type="checkbox"
                            checked={selectedDays.includes(dayOfWeek)}
                            onChange={() => handleDayToggle(dayOfWeek)}
                          />
                          {day}
                        </label>
                      );
                    })}
                  </div>
                </div>

                {selectedDays.length > 0 && (
                  <>
                    <div className="form-group">
                      <label>Режим настройки</label>
                      <div className="schedule-mode-toggle">
                        <button
                          type="button"
                          className={`mode-btn ${scheduleMode === 'all' ? 'active' : ''}`}
                          onClick={() => setScheduleMode('all')}
                        >
                          Для всех дней
                        </button>
                        <button
                          type="button"
                          className={`mode-btn ${scheduleMode === 'individual' ? 'active' : ''}`}
                          onClick={() => setScheduleMode('individual')}
                        >
                          Для каждого дня отдельно
                        </button>
                      </div>
                    </div>

                    {scheduleMode === 'all' ? (
                      // Режим "для всех дней"
                      <>
                        <div className="form-row">
                          <div className="form-group">
                            <label>Начало работы</label>
                            <input
                              type="time"
                              value={scheduleForm[selectedDays[0]]?.startTime || '09:00'}
                              onChange={(e) =>
                                selectedDays.forEach(day =>
                                  handleScheduleFormChange(day, 'startTime', e.target.value)
                                )
                              }
                            />
                          </div>
                          <div className="form-group">
                            <label>Окончание работы</label>
                            <input
                              type="time"
                              value={scheduleForm[selectedDays[0]]?.endTime || '17:00'}
                              onChange={(e) =>
                                selectedDays.forEach(day =>
                                  handleScheduleFormChange(day, 'endTime', e.target.value)
                                )
                              }
                            />
                          </div>
                        </div>
                        <div className="form-group">
                          <label>Длительность приема (минут)</label>
                          <input
                            type="number"
                            min="5"
                            max="120"
                            value={scheduleForm[selectedDays[0]]?.appointmentDuration || 30}
                            onChange={(e) =>
                              selectedDays.forEach(day =>
                                handleScheduleFormChange(day, 'appointmentDuration', parseInt(e.target.value))
                              )
                            }
                          />
                        </div>
                        <div className="form-row">
                          <div className="form-group">
                            <label>Начало перерыва</label>
                            <input
                              type="time"
                              value={scheduleForm[selectedDays[0]]?.lunchStart || '13:00'}
                              onChange={(e) =>
                                selectedDays.forEach(day =>
                                  handleScheduleFormChange(day, 'lunchStart', e.target.value)
                                )
                              }
                            />
                          </div>
                          <div className="form-group">
                            <label>Окончание перерыва</label>
                            <input
                              type="time"
                              value={scheduleForm[selectedDays[0]]?.lunchEnd || '13:30'}
                              onChange={(e) =>
                                selectedDays.forEach(day =>
                                  handleScheduleFormChange(day, 'lunchEnd', e.target.value)
                                )
                              }
                            />
                          </div>
                        </div>
                        <div className="form-group">
                          <label>Дата начала действия расписания</label>
                          <input
                            type="date"
                            value={scheduleForm[selectedDays[0]]?.effectiveFrom || new Date().toISOString().split('T')[0]}
                            onChange={(e) =>
                              selectedDays.forEach(day =>
                                handleScheduleFormChange(day, 'effectiveFrom', e.target.value)
                              )
                            }
                            min={new Date().toISOString().split('T')[0]}
                          />
                        </div>
                        <div className="form-group">
                          <label>Дата конца действия расписания (необязательно)</label>
                          <input
                            type="date"
                            value={scheduleForm[selectedDays[0]]?.effectiveTo || ''}
                            onChange={(e) =>
                              selectedDays.forEach(day =>
                                handleScheduleFormChange(day, 'effectiveTo', e.target.value || undefined)
                              )
                            }
                            min={scheduleForm[selectedDays[0]]?.effectiveFrom || new Date().toISOString().split('T')[0]}
                          />
                          <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '4px', display: 'block' }}>
                            Если не указана, расписание действует бессрочно
                          </small>
                        </div>
                      </>
                    ) : (
                      // Режим "для каждого дня отдельно"
                      <>
                        <div className="form-group">
                          <label>Дата начала действия расписания</label>
                          <input
                            type="date"
                            value={scheduleForm[selectedDays[0]]?.effectiveFrom || new Date().toISOString().split('T')[0]}
                            onChange={(e) =>
                              selectedDays.forEach(day =>
                                handleScheduleFormChange(day, 'effectiveFrom', e.target.value)
                              )
                            }
                            min={new Date().toISOString().split('T')[0]}
                          />
                        </div>
                        <div className="form-group">
                          <label>Дата конца действия расписания (необязательно)</label>
                          <input
                            type="date"
                            value={scheduleForm[selectedDays[0]]?.effectiveTo || ''}
                            onChange={(e) =>
                              selectedDays.forEach(day =>
                                handleScheduleFormChange(day, 'effectiveTo', e.target.value || undefined)
                              )
                            }
                            min={scheduleForm[selectedDays[0]]?.effectiveFrom || new Date().toISOString().split('T')[0]}
                          />
                          <small style={{ color: '#666', fontSize: '0.85rem', marginTop: '4px', display: 'block' }}>
                            Если не указана, расписание действует бессрочно
                          </small>
                        </div>
                        <div className="individual-schedule">
                          {selectedDays.map((dayOfWeek) => {
                            const dayName = dayNamesSchedule[dayOfWeek === 0 ? 6 : dayOfWeek - 1];
                            return (
                              <div key={dayOfWeek} className="day-schedule-block">
                                <h4>{dayName}</h4>
                                <div className="form-row">
                                  <div className="form-group">
                                    <label>Начало</label>
                                    <input
                                      type="time"
                                      value={scheduleForm[dayOfWeek]?.startTime || '09:00'}
                                      onChange={(e) =>
                                        handleScheduleFormChange(dayOfWeek, 'startTime', e.target.value)
                                      }
                                    />
                                  </div>
                                  <div className="form-group">
                                    <label>Окончание</label>
                                    <input
                                      type="time"
                                      value={scheduleForm[dayOfWeek]?.endTime || '17:00'}
                                      onChange={(e) =>
                                        handleScheduleFormChange(dayOfWeek, 'endTime', e.target.value)
                                      }
                                    />
                                  </div>
                                </div>
                                <div className="form-row">
                                  <div className="form-group">
                                    <label>Длительность (мин)</label>
                                    <input
                                      type="number"
                                      min="5"
                                      max="120"
                                      value={scheduleForm[dayOfWeek]?.appointmentDuration || 30}
                                      onChange={(e) =>
                                        handleScheduleFormChange(dayOfWeek, 'appointmentDuration', parseInt(e.target.value))
                                      }
                                    />
                                  </div>
                                </div>
                                <div className="form-row">
                                  <div className="form-group">
                                    <label>Перерыв с</label>
                                    <input
                                      type="time"
                                      value={scheduleForm[dayOfWeek]?.lunchStart || '13:00'}
                                      onChange={(e) =>
                                        handleScheduleFormChange(dayOfWeek, 'lunchStart', e.target.value)
                                      }
                                    />
                                  </div>
                                  <div className="form-group">
                                    <label>до</label>
                                    <input
                                      type="time"
                                      value={scheduleForm[dayOfWeek]?.lunchEnd || '13:30'}
                                      onChange={(e) =>
                                        handleScheduleFormChange(dayOfWeek, 'lunchEnd', e.target.value)
                                      }
                                    />
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      </>
                    )}
                  </>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setShowSettingsModal(false)}>
                  Отмена
                </button>
                <button className="btn btn-primary" onClick={handleSaveSchedule} disabled={loading}>
                  {loading ? 'Сохранение...' : 'Сохранить расписание'}
                </button>
              </div>
            </div>
          </div>
        )}

        {showBlockModal && (
          <div className="modal-overlay" onClick={() => setShowBlockModal(false)}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>{blockModalMode === 'block' ? 'Блокировка даты' : 'Разблокировка даты'}</h2>
                <button className="modal-close" onClick={() => setShowBlockModal(false)}>
                  ✕
                </button>
              </div>
              <div className="modal-body">
                <div className="form-group">
                  <label>Дата начала</label>
                  <input
                    type="date"
                    value={blockForm.startDate}
                    onChange={(e) => setBlockForm({ ...blockForm, startDate: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Дата окончания</label>
                  <input
                    type="date"
                    value={blockForm.endDate}
                    onChange={(e) => setBlockForm({ ...blockForm, endDate: e.target.value })}
                  />
                </div>
                {blockModalMode === 'block' && (
                  <div className="form-group">
                    <label>Причина (необязательно)</label>
                    <input
                      type="text"
                      placeholder="Отпуск, больничный..."
                      value={blockForm.reason}
                      onChange={(e) => setBlockForm({ ...blockForm, reason: e.target.value })}
                    />
                  </div>
                )}
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setShowBlockModal(false)}>
                  Отмена
                </button>
                {blockModalMode === 'block' ? (
                  <button className="btn btn-danger" onClick={handleBlockDate} disabled={loading}>
                    {loading ? 'Блокировка...' : 'Заблокировать'}
                  </button>
                ) : (
                  <button className="btn btn-primary" onClick={handleUnblockDate} disabled={loading}>
                    {loading ? 'Разблокировка...' : 'Разблокировать'}
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default SchedulePage;
