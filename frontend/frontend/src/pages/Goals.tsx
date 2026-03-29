import React, { useEffect, useState } from "react";
import { goalApi } from "../api/goalApi";
import { userApi } from "../api/userApi";
import type { GoalResponse, UserResponse } from "../api/types";
import { useAuth } from "../context/AuthContext";

const Goals: React.FC = () => {
  const { user } = useAuth();
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [employees, setEmployees] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    dueDate: "",
    employeeId: ""
  });

  const fetchData = async () => {
    try {
      const [goalRes, empRes] = await Promise.all([
        user?.role === "HR" ? goalApi.getAll() :
        user?.role === "MANAGER" ? goalApi.getTeam() : goalApi.getMy(),
        user?.role !== "EMPLOYEE" ? userApi.getTeam() : Promise.resolve({ data: { data: [] } })
      ]);
      setGoals(goalRes.data.data);
      setEmployees(empRes.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await goalApi.create({
        ...formData,
        employeeId: Number(formData.employeeId)
      });
      setShowModal(false);
      setFormData({ title: "", description: "", dueDate: "", employeeId: "" });
      fetchData();
    } catch (err) {
      alert("Failed to create goal");
    }
  };

  const handleUpdateProgress = async (id: number, currentProgress: number) => {
    const next = prompt("Enter new progress % (0-100):", currentProgress.toString());
    if (next !== null) {
      const progress = parseInt(next);
      if (!isNaN(progress)) {
        await goalApi.updateProgress(id, progress);
        fetchData();
      }
    }
  };

  if (loading) return <div>Loading goals...</div>;

  return (
    <div className="animate-fade">
      <div className="flex justify-between items-center mb-4">
        <h2>Goals & Objectives</h2>
        {user?.role !== "EMPLOYEE" && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>Assign New Goal</button>
        )}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 100
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '90%' }}>
            <h3>Assign Goal</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Goal Title</label>
                <input type="text" value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} required />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea value={formData.description} onChange={e => setFormData({...formData, description: e.target.value})} rows={3}></textarea>
              </div>
              <div className="grid" style={{ gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Due Date</label>
                  <input type="date" value={formData.dueDate} onChange={e => setFormData({...formData, dueDate: e.target.value})} required />
                </div>
                <div className="form-group">
                  <label>Assign To</label>
                  <select value={formData.employeeId} onChange={e => setFormData({...formData, employeeId: e.target.value})} required>
                    <option value="">Select Employee</option>
                    {employees.map(e => <option key={e.id} value={e.id}>{e.fullName}</option>)}
                  </select>
                </div>
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Assign</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="dashboard-grid">
        {goals.map(g => (
          <div key={g.id} className="glass-card animate-slide">
            <div className="flex justify-between items-start mb-2">
              <h3 style={{ margin: 0, fontSize: '1.1rem' }}>{g.title}</h3>
              <span className={`badge badge-${g.status === 'COMPLETED' ? 'approved' : 'pending'}`}>{g.status}</span>
            </div>
            <p className="subtitle mb-4" style={{ color: 'var(--text-primary)', fontSize: '0.875rem' }}>{g.description}</p>
            
            <div className="mb-4">
              <div className="flex justify-between mb-1 subtitle" style={{ fontSize: '0.75rem' }}>
                <span>Progress</span>
                <span>{g.progress}%</span>
              </div>
              <div style={{ height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', position: 'relative' }}>
                <div style={{ 
                  height: '100%', width: `${g.progress}%`, 
                  background: 'var(--primary)', borderRadius: '4px', transition: 'width 0.4s' 
                }}></div>
              </div>
            </div>

            <div className="flex justify-between items-center">
              <div className="subtitle" style={{ fontSize: '0.75rem' }}>
                Due: {new Date(g.dueDate).toLocaleDateString()}
              </div>
              {user?.id === g.employeeId && (
                <button className="btn btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem' }} 
                        onClick={() => handleUpdateProgress(g.id, g.progress)}>
                  Update
                </button>
              )}
            </div>
            {user?.id !== g.employeeId && (
              <div className="mt-2 subtitle" style={{ fontSize: '0.75rem' }}>Assigned to: {g.employeeName}</div>
            )}
          </div>
        ))}
        {goals.length === 0 && <p className="subtitle">No goals found.</p>}
      </div>
    </div>
  );
};

export default Goals;
