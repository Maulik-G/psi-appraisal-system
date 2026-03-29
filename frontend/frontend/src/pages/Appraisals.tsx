import React, { useEffect, useState } from "react";
import { appraisalApi } from "../api/appraisalApi";
import { userApi } from "../api/userApi";
import type { AppraisalResponse, UserResponse } from "../api/types";
import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";

const Appraisals: React.FC = () => {
  const { user } = useAuth();
  const [appraisals, setAppraisals] = useState<AppraisalResponse[]>([]);
  const [employees, setEmployees] = useState<UserResponse[]>([]);
  const [managers, setManagers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    cycleName: "",
    startDate: "",
    endDate: "",
    employeeId: "",
    managerId: ""
  });

  const fetchData = async () => {
    try {
      const [appRes, empRes, manRes] = await Promise.all([
        user?.role === "HR" ? appraisalApi.getAll() :
        user?.role === "MANAGER" ? appraisalApi.getTeam() : appraisalApi.getMy(),
        user?.role === "HR" ? userApi.getByRole("EMPLOYEE") : Promise.resolve({ data: { data: [] } }),
        user?.role === "HR" ? userApi.getByRole("MANAGER") : Promise.resolve({ data: { data: [] } })
      ]);
      setAppraisals(appRes.data.data);
      if (user?.role === "HR") {
        setEmployees(empRes.data.data);
        setManagers(manRes.data.data);
      }
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
      await appraisalApi.create({
        ...formData,
        employeeId: Number(formData.employeeId),
        managerId: Number(formData.managerId)
      });
      setShowModal(false);
      setFormData({ cycleName: "", startDate: "", endDate: "", employeeId: "", managerId: "" });
      fetchData();
    } catch (err) {
      alert("Failed to create appraisal cycle");
    }
  };

  if (loading) return <div>Loading appraisals...</div>;

  return (
    <div className="animate-fade">
      <div className="flex justify-between items-center mb-4">
        <h2>Appraisals</h2>
        {user?.role === "HR" && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>New Appraisal Cycle</button>
        )}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 100
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '90%' }}>
            <h3>Create Appraisal Cycle</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Cycle Name</label>
                <input type="text" placeholder="e.g. Annual Appraisal 2026" 
                  value={formData.cycleName} onChange={e => setFormData({...formData, cycleName: e.target.value})} required />
              </div>
              <div className="grid" style={{ gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Start Date</label>
                  <input type="date" value={formData.startDate} onChange={e => setFormData({...formData, startDate: e.target.value})} required />
                </div>
                <div className="form-group">
                  <label>End Date</label>
                  <input type="date" value={formData.endDate} onChange={e => setFormData({...formData, endDate: e.target.value})} required />
                </div>
              </div>
              <div className="form-group">
                <label>Employee</label>
                <select value={formData.employeeId} onChange={e => setFormData({...formData, employeeId: e.target.value})} required>
                  <option value="">Select Employee</option>
                  {employees.map(e => <option key={e.id} value={e.id}>{e.fullName} ({e.email})</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Manager</label>
                <select value={formData.managerId} onChange={e => setFormData({...formData, managerId: e.target.value})} required>
                  <option value="">Select Manager</option>
                  {managers.map(m => <option key={m.id} value={m.id}>{m.fullName}</option>)}
                </select>
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="glass-card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Cycle</th>
                <th>Employee</th>
                <th>Manager</th>
                <th>Status</th>
                <th>Period</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appraisals.map(a => (
                <tr key={a.id}>
                  <td>{a.cycleName}</td>
                  <td>{a.employeeName}</td>
                  <td>{a.managerName}</td>
                  <td>
                    <span className={`badge badge-${
                      a.status === 'APPROVED' ? 'approved' : 
                      a.status === 'ACKNOWLEDGED' ? 'approved' : 'pending'
                    }`}>{a.status}</span>
                  </td>
                  <td className="subtitle" style={{ fontSize: '0.75rem' }}>
                    {new Date(a.startDate).toLocaleDateString()} - {new Date(a.endDate).toLocaleDateString()}
                  </td>
                  <td>
                    <Link to={`/appraisals/${a.id}`} className="btn btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.75rem' }}>
                      View Details
                    </Link>
                  </td>
                </tr>
              ))}
              {appraisals.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center' }} className="subtitle">No appraisals found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Appraisals;
