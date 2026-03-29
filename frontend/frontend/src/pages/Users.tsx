import React, { useEffect, useState } from "react";
import { userApi } from "../api/userApi";
import { departmentApi } from "../api/departmentApi";
import type { UserResponse, DepartmentResponse } from "../api/types";

const Users: React.FC = () => {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [managers, setManagers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Create User Form State
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    role: "EMPLOYEE",
    departmentId: "",
    managerId: ""
  });
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState("");

  const fetchData = async () => {
    try {
      const [uRes, dRes] = await Promise.all([
        userApi.getAll(),
        departmentApi.getAll()
      ]);
      setUsers(uRes.data.data);
      setDepartments(dRes.data.data);
      setManagers(uRes.data.data.filter(u => u.role === "MANAGER" || u.role === "HR"));
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await userApi.create({
        ...formData,
        departmentId: formData.departmentId ? Number(formData.departmentId) : undefined,
        managerId: formData.managerId ? Number(formData.managerId) : undefined
      });
      setShowModal(false);
      setFormData({ fullName: "", email: "", password: "", role: "EMPLOYEE", departmentId: "", managerId: "" });
      fetchData();
    } catch (err: any) {
      setError(err.response?.data?.message || "Failed to create user");
    }
  };

  if (loading) return <div>Loading users...</div>;

  return (
    <div className="animate-fade">
      <div className="flex justify-between items-center mb-4">
        <h2>System Users</h2>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>Add New User</button>
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 100
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '90%' }}>
            <h3>Create New User</h3>
            {error && <div className="badge badge-danger mb-4 w-full">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" value={formData.fullName} required
                  onChange={e => setFormData({ ...formData, fullName: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value={formData.email} required
                  onChange={e => setFormData({ ...formData, email: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Password</label>
                <input type="password" value={formData.password} required
                  onChange={e => setFormData({ ...formData, password: e.target.value })} />
              </div>
              <div className="grid" style={{ gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div className="form-group">
                  <label>Role</label>
                  <select value={formData.role} onChange={e => setFormData({ ...formData, role: e.target.value })}>
                    <option value="EMPLOYEE">Employee</option>
                    <option value="MANAGER">Manager</option>
                    <option value="HR">HR</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Department</label>
                  <select value={formData.departmentId} onChange={e => setFormData({ ...formData, departmentId: e.target.value })}>
                    <option value="">Select Department</option>
                    {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Reporting Manager</label>
                <select value={formData.managerId} onChange={e => setFormData({ ...formData, managerId: e.target.value })}>
                  <option value="">No Manager</option>
                  {managers.map(m => <option key={m.id} value={m.id}>{m.fullName} ({m.role})</option>)}
                </select>
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create User</button>
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
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Department</th>
                <th>Manager</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td><span className="badge badge-approved">{u.role}</span></td>
                  <td>{u.departmentName || "-"}</td>
                  <td>{u.managerName || "-"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Users;
