import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { appraisalApi } from "../api/appraisalApi";
import { notificationApi } from "../api/notificationApi";
import { goalApi } from "../api/goalApi";
import type { AppraisalResponse, GoalResponse } from "../api/types";
import { Link } from "react-router-dom";

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [appraisals, setAppraisals] = useState<AppraisalResponse[]>([]);
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [appRes, goalRes, notifRes] = await Promise.all([
          user?.role === "HR" ? appraisalApi.getAll() : 
          user?.role === "MANAGER" ? appraisalApi.getTeam() : appraisalApi.getMy(),
          user?.role === "EMPLOYEE" ? goalApi.getMy() : goalApi.getTeam(),
          notificationApi.getUnreadCount()
        ]);

        setAppraisals(appRes.data.data.slice(0, 5));
        setGoals(goalRes.data.data.slice(0, 5));
        setUnreadCount(notifRes.data.data.count);
      } catch (error) {
        console.error("Dashboard error:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user]);

  if (loading) return <div>Loading dashboard...</div>;

  return (
    <div className="dashboard-grid">
      <div className="glass-card flex justify-between items-center" style={{ gridColumn: 'span 1' }}>
        <div>
          <div className="subtitle">Total Appraisals</div>
          <div style={{ fontSize: '2rem', fontWeight: 700 }}>{appraisals.length}</div>
        </div>
        <div className="badge badge-approved">Active</div>
      </div>

      <div className="glass-card flex justify-between items-center">
        <div>
          <div className="subtitle">System Notifications</div>
          <div style={{ fontSize: '2rem', fontWeight: 700 }}>{unreadCount}</div>
        </div>
        <div className="badge badge-pending">Unread</div>
      </div>

      <div className="glass-card" style={{ gridColumn: 'span 1' }}>
        <h2 style={{ fontSize: '1.25rem' }}>User Profile</h2>
        <div className="flex items-center gap-2 mb-2">
          <div className="subtitle">Role:</div>
          <span className="badge badge-approved">{user?.role}</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="subtitle">Email:</div>
          <div>{user?.email}</div>
        </div>
      </div>

      <div className="glass-card" style={{ gridColumn: 'span 2' }}>
        <div className="flex justify-between items-center mb-4">
          <h2 style={{ margin: 0 }}>Recent Appraisals</h2>
          <Link to="/appraisals" style={{ color: 'var(--primary)', textDecoration: 'none', fontSize: '0.875rem' }}>View All</Link>
        </div>
        {appraisals.length === 0 ? (
          <p className="subtitle">No appraisals found.</p>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Cycle Name</th>
                  <th>Employee</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {appraisals.map((a) => (
                  <tr key={a.id}>
                    <td>{a.cycleName}</td>
                    <td>{a.employeeName}</td>
                    <td><span className={`badge badge-${a.status === 'APPROVED' ? 'approved' : 'pending'}`}>{a.status}</span></td>
                    <td>
                      <Link to={`/appraisals/${a.id}`} style={{ color: 'var(--primary)', textDecoration: 'none' }}>Details</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="glass-card" style={{ gridColumn: 'span 1' }}>
        <h2 style={{ fontSize: '1.25rem' }}>Goals Overview</h2>
        {goals.slice(0, 3).map(goal => (
          <div key={goal.id} className="mb-4">
            <div className="flex justify-between mb-2">
              <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{goal.title}</div>
              <div className="subtitle">{goal.progress}%</div>
            </div>
            <div style={{ height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '3px' }}>
              <div style={{ 
                height: '100%', 
                width: `${goal.progress}%`, 
                background: 'var(--primary)', 
                borderRadius: '3px',
                transition: 'width 0.5s ease-out'
              }}></div>
            </div>
          </div>
        ))}
        {goals.length === 0 && <p className="subtitle">No goals assigned.</p>}
        <Link to="/goals" className="btn btn-secondary" style={{ width: '100%', textDecoration: 'none', marginTop: '1rem' }}>
          Manage Goals
        </Link>
      </div>
    </div>
  );
};

export default Dashboard;
