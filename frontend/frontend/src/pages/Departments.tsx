import React, { useEffect, useState } from "react";
import { departmentApi } from "../api/departmentApi";
import type { DepartmentResponse } from "../api/types";

const Departments: React.FC = () => {
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const fetchDepts = async () => {
    try {
      const res = await departmentApi.getAll();
      setDepartments(res.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepts();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await departmentApi.create({ name, description });
      setName("");
      setDescription("");
      setShowModal(false);
      fetchDepts();
    } catch (err) {
      alert("Failed to create department");
    }
  };

  const handleDelete = async (id: number) => {
    if (window.confirm("Delete this department?")) {
      await departmentApi.delete(id);
      fetchDepts();
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="animate-fade">
      <div className="flex justify-between items-center mb-4">
        <h2>Departments</h2>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>Add Department</button>
      </div>

      {showModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center',
          zIndex: 100
        }}>
          <div className="glass-card" style={{ maxWidth: '400px', width: '90%' }}>
            <h3>New Department</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Name</label>
                <input type="text" value={name} onChange={e => setName(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea value={description} onChange={e => setDescription(e.target.value)} rows={3}></textarea>
              </div>
              <div className="flex gap-2 justify-end">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <div className="dashboard-grid">
        {departments.map(d => (
          <div key={d.id} className="glass-card animate-slide">
            <div className="flex justify-between items-start mb-2">
              <h3 style={{ margin: 0 }}>{d.name}</h3>
              <button onClick={() => handleDelete(d.id)} className="badge badge-danger" style={{ border: 'none', cursor: 'pointer' }}>Delete</button>
            </div>
            <p className="subtitle">{d.description || "No description provided."}</p>
            <div className="mt-4 subtitle" style={{ fontSize: '0.75rem' }}>Created: {new Date(d.createdAt).toLocaleDateString()}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Departments;
