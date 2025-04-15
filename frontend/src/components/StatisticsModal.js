// src/components/StatisticsModal.js
import React from 'react';
import { Modal } from 'react-bootstrap';
import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
} from 'recharts';

function StatisticsModal({ show, onHide, data }) {
    return (
        <Modal show={show} onHide={onHide} centered>
            <Modal.Header closeButton>
                <Modal.Title>연령대 통계</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {data.length === 0 ? (
                    <p className="text-center text-muted">통계 데이터가 없습니다.</p>
                ) : (
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={data}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis allowDecimals={false} />
                            <Tooltip />
                            <Bar dataKey="count" fill="#8884d8" />
                        </BarChart>
                    </ResponsiveContainer>
                )}
            </Modal.Body>
        </Modal>
    );
}

export default StatisticsModal;
