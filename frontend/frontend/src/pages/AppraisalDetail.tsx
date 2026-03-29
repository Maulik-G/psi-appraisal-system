import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { appraisalApi } from "../api/appraisalApi";
import { feedbackApi } from "../api/feedbackApi";
import { useAuth } from "../context/AuthContext";
import type { AppraisalResponse, FeedbackResponse } from "../api/types";

const AppraisalDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [appraisal, setAppraisal] = useState<AppraisalResponse | null>(null);
  const [feedbacks, setFeedbacks] = useState<FeedbackResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [selfAssessment, setSelfAssessment] = useState("");
  const [managerComment, setManagerComment] = useState("");
  const [rating, setRating] = useState(5);
  const [feedbackComment, setFeedbackComment] = useState("");

  const fetchData = async () => {
    if (!id) return;
    try {
      const [appRes, feedRes] = await Promise.all([
        appraisalApi.getById(Number(id)),
        feedbackApi.getByAppraisal(Number(id))
      ]);
      setAppraisal(appRes.data.data);
      setFeedbacks(feedRes.data.data);
      setSelfAssessment(appRes.data.data.selfAssessment || "");
      setManagerComment(appRes.data.data.managerComment || "");
      if (appRes.data.data.rating) setRating(appRes.data.data.rating);
    } catch (err) {
      console.error(err);
      navigate("/appraisals");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [id]);

  const handleSelfSubmit = async () => {
    if (!id) return;
    try {
      await appraisalApi.submitSelfAssessment(Number(id), selfAssessment);
      fetchData();
    } catch (err) {
      alert("Error submitting self-assessment");
    }
  };

  const handleManagerSubmit = async () => {
    if (!id) return;
    try {
      await appraisalApi.managerReview(Number(id), managerComment, rating);
      fetchData();
    } catch (err) {
      alert("Error submitting manager review");
    }
  };

  const handleApprove = async () => {
    if (!id) return;
    try {
      await appraisalApi.approve(Number(id));
      fetchData();
    } catch (err) {
      alert("Error approving appraisal");
    }
  };

  const handleAcknowledge = async () => {
    if (!id) return;
    try {
      await appraisalApi.acknowledge(Number(id));
      fetchData();
    } catch (err) {
      alert("Error acknowledging appraisal");
    }
  };

  const handleAddFeedback = async (type: string) => {
    if (!id) return;
    try {
      await feedbackApi.create({
        appraisalId: Number(id),
        type,
        comment: feedbackComment,
        rating: type === 'PEER' ? 4 : undefined
      });
      setFeedbackComment("");
      fetchData();
    } catch (err) {
      alert("Error submitting feedback (likely duplicate)");
    }
  };

  if (loading || !appraisal) return <div>Loading appraisal...</div>;

  const isEmployee = user?.id === appraisal.employeeId;
  const isManager = user?.id === appraisal.managerId;
  const isHR = user?.role === "HR";

  return (
    <div className="animate-fade grid" style={{ gridTemplateColumns: '1.5fr 1fr', gap: '2rem' }}>
      <div className="flex flex-col gap-4">
        <div className="glass-card">
          <div className="flex justify-between items-start mb-4">
            <div>
              <h2 style={{ margin: 0 }}>{appraisal.cycleName}</h2>
              <p className="subtitle">{appraisal.employeeName} ({appraisal.status})</p>
            </div>
            <span className={`badge badge-${appraisal.status.includes('APPROVED') ? 'approved' : 'pending'}`}>{appraisal.status}</span>
          </div>
          
          <div className="grid mb-4" style={{ gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="glass-card" style={{ background: 'rgba(255,255,255,0.03)' }}>
              <div className="subtitle">Start Date</div>
              <div>{new Date(appraisal.startDate).toDateString()}</div>
            </div>
            <div className="glass-card" style={{ background: 'rgba(255,255,255,0.03)' }}>
              <div className="subtitle">End Date</div>
              <div>{new Date(appraisal.endDate).toDateString()}</div>
            </div>
          </div>
        </div>

        {/* Workflow Steps */}
        <div className="glass-card">
          <h3 className="mb-4">Appraisal Journey</h3>
          
          {/* Step 1: Self Assessment */}
          <div className="mb-6 pb-6" style={{ borderBottom: '1px solid var(--border)' }}>
            <h4 className="mb-2">1. Self Assessment</h4>
            {appraisal.status === 'PENDING' && isEmployee ? (
              <div>
                <textarea 
                  className="mb-2" rows={4} placeholder="Reflect on your achievements and areas of growth..."
                  value={selfAssessment} onChange={e => setSelfAssessment(e.target.value)}
                ></textarea>
                <button className="btn btn-primary" onClick={handleSelfSubmit}>Submit Self Assessment</button>
              </div>
            ) : (
              <div className="glass-card" style={{ background: 'rgba(255,255,255,0.02)' }}>
                {appraisal.selfAssessment || <span className="subtitle italic">Not yet submitted</span>}
              </div>
            )}
          </div>

          {/* Step 2: Manager Review */}
          <div className="mb-6 pb-6" style={{ borderBottom: '1px solid var(--border)' }}>
            <h4 className="mb-2">2. Manager Review</h4>
            {appraisal.status === 'SELF_SUBMITTED' && (isManager || isHR) ? (
              <div>
                <label>Rating (1-5)</label>
                <select className="mb-2" value={rating} onChange={e => setRating(Number(e.target.value))}>
                  {[1,2,3,4,5].map(v => <option key={v} value={v}>{v}</option>)}
                </select>
                <textarea 
                  className="mb-2" rows={4} placeholder="Manager's feedback and evaluation..."
                  value={managerComment} onChange={e => setManagerComment(e.target.value)}
                ></textarea>
                <button className="btn btn-primary" onClick={handleManagerSubmit}>Submit Review</button>
              </div>
            ) : (
              <div className="glass-card" style={{ background: 'rgba(255,255,255,0.02)' }}>
                {appraisal.rating && <div className="badge badge-approved mb-2">Rating: {appraisal.rating} / 5</div>}
                <div>{appraisal.managerComment || <span className="subtitle italic">Evaluation pending</span>}</div>
              </div>
            )}
          </div>

          {/* Step 3: Approval & Acknowledge */}
          <div>
            <h4 className="mb-2">3. Finalization</h4>
            {appraisal.status === 'MANAGER_REVIEWED' && isHR && (
              <div className="flex gap-2">
                <button className="btn btn-primary" onClick={handleApprove}>Approve Appraisal</button>
              </div>
            )}
            {appraisal.status === 'APPROVED' && isEmployee && (
              <button className="btn btn-primary" onClick={handleAcknowledge}>Acknowledge Results</button>
            )}
            {appraisal.status === 'ACKNOWLEDGED' && (
              <div className="badge badge-approved" style={{ padding: '1rem', width: '100%', textAlign: 'center' }}>
                COMPLETED & ACKNOWLEDGED
              </div>
            )}
            {appraisal.status !== 'ACKNOWLEDGED' && appraisal.status !== 'APPROVED' && appraisal.status !== 'MANAGER_REVIEWED' && (
              <p className="subtitle italic">Waiting for previous steps...</p>
            )}
          </div>
        </div>
      </div>

      {/* Side Panel: Feedbacks */}
      <div className="flex flex-col gap-4">
        <div className="glass-card">
          <h3 className="mb-4">360° Feedback</h3>
          <div className="flex flex-col gap-2 mb-4">
            {feedbacks.map(f => (
              <div key={f.id} className="glass-card" style={{ padding: '1rem', fontSize: '0.875rem', background: 'rgba(255,255,255,0.03)' }}>
                <div className="flex justify-between mb-1">
                  <span style={{ fontWeight: 600 }}>{f.reviewerName}</span>
                  <span className="badge badge-approved">{f.type}</span>
                </div>
                <p className="subtitle" style={{ color: 'var(--text-primary)' }}>"{f.comment}"</p>
                <div style={{ fontSize: '10px' }} className="mt-1 subtitle">{new Date(f.createdAt).toLocaleDateString()}</div>
              </div>
            ))}
          </div>

          {(isEmployee || isManager || isHR) && (
            <div className="mt-4">
              <textarea 
                placeholder="Write feedback..." rows={3} className="mb-2"
                value={feedbackComment} onChange={e => setFeedbackComment(e.target.value)}
              ></textarea>
              <div className="flex gap-2">
                <button className="btn btn-secondary" style={{ flex: 1, fontSize: '0.75rem' }} onClick={() => handleAddFeedback('PEER')}>Add Peer</button>
                {isHR && <button className="btn btn-primary" style={{ flex: 1, fontSize: '0.75rem' }} onClick={() => handleAddFeedback('MANAGER')}>Official Note</button>}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AppraisalDetail;
