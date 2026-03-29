import React, { useEffect, useState } from "react";
import { notificationApi } from "../api/notificationApi";
import type { NotificationResponse } from "../api/types";

const Notifications: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = async () => {
    try {
      const res = await notificationApi.getAll();
      setNotifications(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkRead = async (id: number) => {
    await notificationApi.markAsRead(id);
    fetchNotifications();
  };

  const handleReadAll = async () => {
    await notificationApi.markAllAsRead();
    fetchNotifications();
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="animate-fade" style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div className="flex justify-between items-center mb-4">
        <h2>Notifications</h2>
        {notifications.some(n => !n.read) && (
          <button className="btn btn-secondary" style={{ fontSize: '0.75rem' }} onClick={handleReadAll}>Mark all as read</button>
        )}
      </div>

      <div className="flex flex-col gap-3">
        {notifications.map(n => (
          <div key={n.id} className="glass-card flex justify-between items-center" 
               style={{ 
                 opacity: n.read ? 0.6 : 1, 
                 borderLeft: n.read ? '1px solid var(--border)' : '4px solid var(--primary)',
                 transition: 'all 0.2s'
               }}>
            <div>
              <p style={{ margin: 0, fontWeight: n.read ? 400 : 600 }}>{n.message}</p>
              <div className="subtitle" style={{ fontSize: '0.75rem', marginTop: '0.25rem' }}>
                {new Date(n.createdAt).toLocaleString()}
              </div>
            </div>
            {!n.read && (
              <button 
                className="btn btn-secondary" 
                style={{ padding: '0.3rem 0.6rem', fontSize: '10px' }}
                onClick={() => handleMarkRead(n.id)}
              >
                Mark Read
              </button>
            )}
          </div>
        ))}
        {notifications.length === 0 && (
          <div className="glass-card subtitle" style={{ textAlign: 'center' }}>
            No notifications yet.
          </div>
        )}
      </div>
    </div>
  );
};

export default Notifications;
