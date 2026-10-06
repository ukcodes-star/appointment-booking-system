import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { serviceApi, appointmentApi } from '../api/client';
import type { ServiceEntity, SlotItem, UserAppointment, ServiceType } from '../types';
import { useAuth } from '../context/AuthContext';
import { Calendar, Clock, Check, AlertTriangle, Layers, UserCheck } from 'lucide-react';

const SERVICE_TYPES: (ServiceType | 'ALL')[] = [
  'ALL',
  'MEDICAL',
  'HOUSE_HELP',
  'BEAUTY',
  'FITNESS',
  'EDUCATION',
  'OTHER',
];

export const UserDashboard: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [tab, setTab] = useState<'browse' | 'my-bookings'>('browse');

  // Browse state
  const [services, setServices] = useState<ServiceEntity[]>([]);
  const [selectedType, setSelectedType] = useState<ServiceType | 'ALL'>('ALL');
  const [selectedService, setSelectedService] = useState<ServiceEntity | null>(null);

  // Date & slots state
  const todayStr = new Date().toISOString().split('T')[0];
  const [selectedDate, setSelectedDate] = useState<string>(todayStr);
  const [slots, setSlots] = useState<SlotItem[]>([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [bookingMessage, setBookingMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  // My Appointments state
  const [myAppointments, setMyAppointments] = useState<UserAppointment[]>([]);

  // Fetch services on filter change
  useEffect(() => {
    const fetchServices = async () => {
      try {
        const typeParam = selectedType === 'ALL' ? undefined : selectedType;
        const res = await serviceApi.getAll(typeParam);
        setServices(res.data);
      } catch (err) {
        console.error('Error fetching services', err);
      }
    };
    fetchServices();
  }, [selectedType]);

  // Fetch dynamic slots
  const fetchSlots = async (serviceId: string, date: string) => {
    setLoadingSlots(true);
    setBookingMessage(null);
    try {
      const res = await serviceApi.getSlots(serviceId, date);
      setSlots(res.data.slots);
    } catch (err: unknown) {
      setSlots([]);
      const msg = axios.isAxiosError(err) ? err.response?.data?.message : null;
      setBookingMessage({
        type: 'error',
        text: msg || 'Could not load slots for this date.',
      });
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleSelectService = (service: ServiceEntity) => {
    setSelectedService(service);
    fetchSlots(service.id, selectedDate);
  };

  const handleDateChange = (date: string) => {
    setSelectedDate(date);
    if (selectedService) {
      fetchSlots(selectedService.id, date);
    }
  };

  // Book appointment
  const handleBookSlot = async (slotId: string) => {
    if (!isAuthenticated) {
      setBookingMessage({ type: 'error', text: 'Please sign in to book an appointment.' });
      return;
    }
    setBookingMessage(null);
    try {
      await appointmentApi.book(slotId);
      setBookingMessage({ type: 'success', text: 'Appointment booked successfully!' });
      // Refresh slots immediately to remove the booked slot
      if (selectedService) {
        fetchSlots(selectedService.id, selectedDate);
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.status === 409) {
        setBookingMessage({
          type: 'error',
          text: 'This slot was just booked by another user! Please pick another slot.',
        });
      } else {
        const msg = axios.isAxiosError(err) ? err.response?.data?.message : null;
        setBookingMessage({
          type: 'error',
          text: msg || 'Failed to book slot.',
        });
      }
    }
  };

  // Load user's booked appointments
  const loadMyAppointments = async () => {
    try {
      const res = await appointmentApi.getMine();
      setMyAppointments(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      {/* Top Tabs */}
      <div className="flex gap-3 mb-8 border-b border-slate-200 pb-4">
        <button
          onClick={() => setTab('browse')}
          className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
            tab === 'browse' ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-100'
          }`}
        >
          <Layers className="w-4 h-4 inline mr-2" />
          Browse Services & Derive Slots
        </button>
        {isAuthenticated && (
          <button
            onClick={() => {
              setTab('my-bookings');
              loadMyAppointments();
            }}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
              tab === 'my-bookings' ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-100'
            }`}
          >
            <UserCheck className="w-4 h-4 inline mr-2" />
            My Booked Appointments
          </button>
        )}
      </div>

      {tab === 'browse' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column: Filter & Services List */}
          <div className="lg:col-span-2 space-y-6">
            {/* Filter Pills */}
            <div className="flex flex-wrap gap-2">
              {SERVICE_TYPES.map((t) => (
                <button
                  key={t}
                  onClick={() => setSelectedType(t)}
                  className={`px-3 py-1.5 rounded-full text-xs font-semibold transition ${
                    selectedType === t
                      ? 'bg-indigo-600 text-white'
                      : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>

            {/* Services Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {services.map((s) => (
                <div
                  key={s.id}
                  onClick={() => handleSelectService(s)}
                  className={`p-5 rounded-xl border cursor-pointer transition ${
                    selectedService?.id === s.id
                      ? 'border-indigo-600 bg-indigo-50/40 shadow-sm'
                      : 'border-slate-200 bg-white hover:border-slate-300 hover:shadow-xs'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-semibold text-slate-900">{s.name}</h3>
                      <p className="text-xs text-slate-500 mt-0.5">By {s.providerName}</p>
                    </div>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 border border-slate-200">
                      {s.type}
                    </span>
                  </div>

                  <div className="mt-4 flex items-center justify-between text-xs text-slate-600">
                    <span className="flex items-center gap-1 font-medium">
                      <Clock className="w-3.5 h-3.5 text-indigo-500" />
                      {s.durationMinutes} mins slot
                    </span>
                    <span className="text-indigo-600 font-semibold hover:underline">Select &rarr;</span>
                  </div>
                </div>
              ))}
              {services.length === 0 && (
                <div className="col-span-2 p-8 text-center bg-white rounded-xl border border-slate-200 text-slate-500 text-sm">
                  No services found for this category.
                </div>
              )}
            </div>
          </div>

          {/* Right Column: Dynamic Slots Panel */}
          <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm h-fit sticky top-24">
            <h3 className="font-bold text-slate-900 text-lg mb-1">Pick a Date & Book</h3>
            <p className="text-xs text-slate-500 mb-4">Slots are dynamically generated on the fly.</p>

            {selectedService ? (
              <div className="space-y-4">
                <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs">
                  <p className="font-semibold text-slate-800">{selectedService.name}</p>
                  <p className="text-slate-500">Duration: {selectedService.durationMinutes} minutes</p>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Select Date</label>
                  <input
                    type="date"
                    min={todayStr}
                    value={selectedDate}
                    onChange={(e) => handleDateChange(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:outline-hidden focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                {bookingMessage && (
                  <div
                    className={`p-3 rounded-lg text-xs flex items-center gap-2 ${
                      bookingMessage.type === 'success'
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : 'bg-red-50 text-red-600 border border-red-200'
                    }`}
                  >
                    {bookingMessage.type === 'success' ? (
                      <Check className="w-4 h-4 shrink-0" />
                    ) : (
                      <AlertTriangle className="w-4 h-4 shrink-0" />
                    )}
                    {bookingMessage.text}
                  </div>
                )}

                <div>
                  <h4 className="text-xs font-semibold text-slate-700 mb-2">Available Slots ({slots.length})</h4>
                  {loadingSlots ? (
                    <p className="text-xs text-slate-400 py-4 text-center">Deriving slots...</p>
                  ) : slots.length > 0 ? (
                    <div className="grid grid-cols-2 gap-2 max-h-64 overflow-y-auto pr-1">
                      {slots.map((slot) => (
                        <button
                          key={slot.slotId}
                          onClick={() => handleBookSlot(slot.slotId)}
                          className="py-2 px-3 border border-indigo-200 rounded-lg bg-indigo-50/50 hover:bg-indigo-600 hover:text-white text-xs font-semibold text-indigo-700 transition text-center"
                        >
                          {slot.startTime} - {slot.endTime}
                        </button>
                      ))}
                    </div>
                  ) : (
                    <div className="p-4 bg-slate-50 border border-slate-200 rounded-lg text-center text-xs text-slate-500">
                      No availability or working hours on this date.
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div className="text-center py-12 text-slate-400 text-xs">
                <Calendar className="w-8 h-8 mx-auto mb-2 text-slate-300" />
                Select a service from the left to view available slots.
              </div>
            )}
          </div>
        </div>
      ) : (
        /* My Bookings View */
        <div className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 mb-4">My Booked Appointments</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {myAppointments.map((app) => (
              <div key={app.id} className="p-5 bg-white border border-slate-200 rounded-xl shadow-xs">
                <div className="flex items-start justify-between">
                  <h4 className="font-semibold text-slate-900">{app.serviceName}</h4>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700 border border-emerald-200">
                    {app.status}
                  </span>
                </div>
                <div className="mt-3 text-xs text-slate-600 space-y-1">
                  <p>
                    <span className="text-slate-400">Date:</span> {app.date}
                  </p>
                  <p>
                    <span className="text-slate-400">Time:</span> {app.startTime} - {app.endTime}
                  </p>
                  <p>
                    <span className="text-slate-400">Type:</span> {app.type}
                  </p>
                </div>
              </div>
            ))}
            {myAppointments.length === 0 && (
              <div className="col-span-3 p-12 text-center bg-white rounded-xl border border-slate-200 text-slate-500 text-sm">
                You have no booked appointments yet.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};