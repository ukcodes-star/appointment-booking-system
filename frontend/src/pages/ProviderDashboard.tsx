import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { serviceApi, providerApi } from '../api/client';
import type { ServiceEntity, ProviderServiceSchedule, ServiceType } from '../types';
import { PlusCircle, Clock, Calendar, CheckCircle2, AlertTriangle, Users } from 'lucide-react';

export const ProviderDashboard: React.FC = () => {
  const [tab, setTab] = useState<'create' | 'availability' | 'schedule'>('schedule');

  // Services owned by provider
  const [services, setServices] = useState<ServiceEntity[]>([]);

  // 1. Create service state
  const [name, setName] = useState('');
  const [type, setType] = useState<ServiceType>('MEDICAL');
  const [durationMinutes, setDurationMinutes] = useState(30);
  const [createMsg, setCreateMsg] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  // 2. Availability state
  const [selectedServiceId, setSelectedServiceId] = useState('');
  const [dayOfWeek, setDayOfWeek] = useState(1);
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('17:00');
  const [availMsg, setAvailMsg] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  // 3. Schedule state
  const todayStr = new Date().toISOString().split('T')[0];
  const [scheduleDate, setScheduleDate] = useState(todayStr);
  const [schedule, setSchedule] = useState<ProviderServiceSchedule[]>([]);

  const loadServices = async () => {
    try {
      const res = await serviceApi.getAll();
      setServices(res.data);
      if (res.data.length > 0 && !selectedServiceId) {
        setSelectedServiceId(res.data[0].id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const loadSchedule = async (date: string) => {
    try {
      const res = await providerApi.getSchedule(date);
      setSchedule(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    let ignore = false;
    async function init() {
      try {
        const [servicesRes, schedRes] = await Promise.all([
          serviceApi.getAll(),
          providerApi.getSchedule(scheduleDate),
        ]);
        if (!ignore) {
          setServices(servicesRes.data);
          if (servicesRes.data.length > 0) {
            setSelectedServiceId(servicesRes.data[0].id);
          }
          setSchedule(schedRes.data);
        }
      } catch (err) {
        console.error(err);
      }
    }
    init();
    return () => {
      ignore = true;
    };
  }, [scheduleDate]);

  const handleCreateService = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateMsg(null);
    try {
      await serviceApi.create({ name, type, durationMinutes });
      setCreateMsg({ type: 'ok', text: 'Service created successfully!' });
      setName('');
      loadServices();
    } catch (err: unknown) {
      const msg = axios.isAxiosError(err) ? err.response?.data?.message : null;
      setCreateMsg({ type: 'err', text: msg || 'Failed to create service' });
    }
  };

  const handleSetAvailability = async (e: React.FormEvent) => {
    e.preventDefault();
    setAvailMsg(null);
    try {
      await serviceApi.setAvailability(selectedServiceId, { dayOfWeek, startTime, endTime });
      setAvailMsg({ type: 'ok', text: 'Weekly availability window saved!' });
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setAvailMsg({ type: 'err', text: 'Error: Overlapping availability window already exists for this day!' });
      } else {
        const msg = axios.isAxiosError(err) ? err.response?.data?.message : null;
        setAvailMsg({ type: 'err', text: msg || 'Failed to set availability' });
      }
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      {/* Tab Switcher */}
      <div className="flex gap-3 mb-8 border-b border-slate-200 pb-4">
        <button
          onClick={() => setTab('schedule')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
            tab === 'schedule' ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-100'
          }`}
        >
          <Calendar className="w-4 h-4 inline mr-2" />
          Daily Schedule
        </button>
        <button
          onClick={() => setTab('create')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
            tab === 'create' ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-100'
          }`}
        >
          <PlusCircle className="w-4 h-4 inline mr-2" />
          Create Service
        </button>
        <button
          onClick={() => setTab('availability')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
            tab === 'availability' ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-100'
          }`}
        >
          <Clock className="w-4 h-4 inline mr-2" />
          Set Weekly Availability
        </button>
      </div>

      {/* 1. Daily Schedule View */}
      {tab === 'schedule' && (
        <div className="space-y-6">
          <div className="flex items-center justify-between bg-white p-4 rounded-xl border border-slate-200">
            <div>
              <h2 className="font-bold text-slate-900 text-lg">Provider Daily Schedule</h2>
              <p className="text-xs text-slate-500">Grouped by service and sorted chronologically.</p>
            </div>
            <div className="flex items-center gap-2">
              <label className="text-xs font-semibold text-slate-700">Date:</label>
              <input
                type="date"
                value={scheduleDate}
                onChange={(e) => {
                  setScheduleDate(e.target.value);
                  loadSchedule(e.target.value);
                }}
                className="px-3 py-1.5 border border-slate-200 rounded-lg text-xs"
              />
            </div>
          </div>

          <div className="space-y-6">
            {schedule.map((group) => (
              <div key={group.serviceId} className="bg-white border border-slate-200 rounded-xl p-6 shadow-xs">
                <div className="flex items-center justify-between border-b border-slate-100 pb-3 mb-4">
                  <h3 className="font-bold text-slate-900 text-base">{group.serviceName}</h3>
                  <span className="text-xs font-semibold px-2 py-0.5 rounded-md bg-slate-100 text-slate-600">
                    {group.type}
                  </span>
                </div>

                {group.appointments.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                    {group.appointments.map((app) => (
                      <div key={app.id} className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs">
                        <div className="flex justify-between items-center mb-1">
                          <span className="font-semibold text-indigo-700">
                            {app.startTime} - {app.endTime}
                          </span>
                          <span className="px-1.5 py-0.5 rounded-xs bg-emerald-100 text-emerald-800 text-[10px] font-bold">
                            {app.status}
                          </span>
                        </div>
                        <p className="text-slate-600 flex items-center gap-1 mt-2">
                          <Users className="w-3.5 h-3.5 text-slate-400" />
                          Patient: <strong className="text-slate-800">{app.patientName}</strong>
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-400 italic">No booked appointments for this service on this date.</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 2. Create Service View */}
      {tab === 'create' && (
        <div className="max-w-lg bg-white border border-slate-200 rounded-2xl p-8 shadow-xs">
          <h2 className="text-xl font-bold text-slate-900 mb-4">Create New Service</h2>
          {createMsg && (
            <div
              className={`p-3 rounded-lg text-xs mb-4 flex items-center gap-2 ${
                createMsg.type === 'ok' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-600'
              }`}
            >
              {createMsg.type === 'ok' ? <CheckCircle2 className="w-4 h-4" /> : <AlertTriangle className="w-4 h-4" />}
              {createMsg.text}
            </div>
          )}
          <form onSubmit={handleCreateService} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Service Name</label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Dental Consultation"
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Service Type</label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value as ServiceType)}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
              >
                <option value="MEDICAL">MEDICAL</option>
                <option value="HOUSE_HELP">HOUSE_HELP</option>
                <option value="BEAUTY">BEAUTY</option>
                <option value="FITNESS">FITNESS</option>
                <option value="EDUCATION">EDUCATION</option>
                <option value="OTHER">OTHER</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">Duration</label>
              <div className="grid grid-cols-4 gap-2">
                {[30, 60, 90, 120].map((d) => (
                  <button
                    key={d}
                    type="button"
                    onClick={() => setDurationMinutes(d)}
                    className={`py-2 text-xs font-bold rounded-lg border transition ${
                      durationMinutes === d
                        ? 'border-indigo-600 bg-indigo-50 text-indigo-700'
                        : 'border-slate-200 bg-white text-slate-600'
                    }`}
                  >
                    {d} min
                  </button>
                ))}
              </div>
            </div>
            <button
              type="submit"
              className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg text-sm mt-4"
            >
              Create Service
            </button>
          </form>
        </div>
      )}

      {/* 3. Set Availability View */}
      {tab === 'availability' && (
        <div className="max-w-lg bg-white border border-slate-200 rounded-2xl p-8 shadow-xs">
          <h2 className="text-xl font-bold text-slate-900 mb-4">Set Weekly Recurring Availability</h2>
          {availMsg && (
            <div
              className={`p-3 rounded-lg text-xs mb-4 flex items-center gap-2 ${
                availMsg.type === 'ok' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-600'
              }`}
            >
              {availMsg.type === 'ok' ? <CheckCircle2 className="w-4 h-4" /> : <AlertTriangle className="w-4 h-4" />}
              {availMsg.text}
            </div>
          )}
          <form onSubmit={handleSetAvailability} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Select Service</label>
              <select
                value={selectedServiceId}
                onChange={(e) => setSelectedServiceId(e.target.value)}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
              >
                {services.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} ({s.durationMinutes}m)
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Day of Week</label>
              <select
                value={dayOfWeek}
                onChange={(e) => setDayOfWeek(Number(e.target.value))}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
              >
                <option value={0}>Sunday (0)</option>
                <option value={1}>Monday (1)</option>
                <option value={2}>Tuesday (2)</option>
                <option value={3}>Wednesday (3)</option>
                <option value={4}>Thursday (4)</option>
                <option value={5}>Friday (5)</option>
                <option value={6}>Saturday (6)</option>
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Start Time (HH:MM)</label>
                <select
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                >
                  {['08:00', '08:30', '09:00', '09:30', '10:00', '10:30', '11:00', '11:30', '12:00', '13:00', '14:00'].map(
                    (t) => (
                      <option key={t} value={t}>
                        {t}
                      </option>
                    )
                  )}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">End Time (HH:MM)</label>
                <select
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm"
                >
                  {['12:00', '13:00', '14:00', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30', '18:00'].map(
                    (t) => (
                      <option key={t} value={t}>
                        {t}
                      </option>
                    )
                  )}
                </select>
              </div>
            </div>
            <button
              type="submit"
              className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg text-sm mt-4"
            >
              Save Availability
            </button>
          </form>
        </div>
      )}
    </div>
  );
};