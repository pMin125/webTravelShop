import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ChatRoom from "../components/ChatRoom";
import StatisticsModal from '../components/StatisticsModal';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { toast } from 'react-toastify';
/* global IMP */

function ProductDetail() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [joinedCount, setJoinedCount] = useState(0);
    const [participationStatus, setParticipationStatus] = useState(null);
    const [ageStats, setAgeStats] = useState([]);
    const [showChat, setShowChat] = useState(false);
    const [showStats, setShowStats] = useState(false);
    const [stompClient, setStompClient] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [timeLeft, setTimeLeft] = useState(0);
    const nickName = localStorage.getItem('nickname');
    const userName = localStorage.getItem('username');
    const navigate = useNavigate();

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    const WS_BASE =
        process.env.NODE_ENV === "development"
            ? "http://localhost:8080/ws"
            : "/ws";

    const deadline = new Date(Date.now() + timeLeft * 1000);
    const pad = (n) => String(n).padStart(2, '0');
    const deadlineText = `${pad(deadline.getHours())}시 ${pad(deadline.getMinutes())}분까지 결제해주세요.`;

    useEffect(() => {
        fetchProductDetail();
        reloadParticipationInfo();
    }, [id]);

    useEffect(() => {
        if (participationStatus === 'WAITING_PAYMENT' && timeLeft > 0) {
            const timeout = setTimeout(() => window.location.reload(), timeLeft * 1000);
            return () => clearTimeout(timeout);
        }
    }, [participationStatus, timeLeft]);

    useEffect(() => {
        const socket = new SockJS(WS_BASE);
        const client = Stomp.over(socket);
        client.debug = () => {};

        client.connect(
            {
                Authorization: `Bearer ${localStorage.getItem('onion_token')}`
            },
            () => {
                client.subscribe(`/sub/notify/${id}`, (msg) => {
                    const newMessage = JSON.parse(msg.body);
                    if (newMessage.type === 'WAITING_NOTIFY' && newMessage.sender === userName) {
                        toast.success("🎉 여행에 합류하게 되었어요!", { autoClose: 8000, pauseOnHover: true });
                        reloadParticipationInfo();
                    }
                    if (newMessage.type === 'UPDATE') {
                        toast.success("🎉 여행인원이 업데이트 되었습니다!", { autoClose: 8000, pauseOnHover: true });
                        reloadParticipationInfo();
                    }
                });
                setStompClient(client);
            }
        );

        return () => {
            if (client) client.disconnect();
        };
    }, [id, userName]);

    useEffect(() => {
        const fetchTTL = async () => {
            try {
                const res = await fetch(`${API_BASE}/participant/payment-ttl?productId=${id}`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                    }
                });
                const data = await res.json();
                setTimeLeft(data.remainingSeconds);
            } catch (err) {
                console.error("TTL 불러오기 실패:", err);
            }
        };

        if (participationStatus === 'WAITING_PAYMENT') {
            fetchTTL();
        }
    }, [participationStatus]);

    const fetchProductDetail = async () => {
        try {
            const res = await fetch(`${API_BASE}/product/products/${id}`);
            const data = await res.json();
            setProduct(data);
        } catch (err) {
            console.error('상품 정보 불러오기 실패:', err);
        }
    };

    const reloadParticipationInfo = async () => {
        try {
            const statusRes = await fetch(`${API_BASE}/participant/status/${id}`, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                }
            });
            const statusData = await statusRes.json();
            setParticipationStatus(statusData.status ?? "NONE");

            const summaryRes = await fetch(`${API_BASE}/participant/summary/${id}`);
            const summaryData = await summaryRes.json();

            const chartData = Object.entries(summaryData.ageStats).map(([key, value]) => ({
                name: key,
                count: value
            }));

            setAgeStats(chartData);
            setJoinedCount(summaryData.joinedCount);
        } catch (err) {
            console.error("❌ reloadParticipationInfo 실패:", err);
        }
    };

    const handleJoin = async () => {
        try {
            const res = await fetch(`${API_BASE}/participant/${id}`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                }
            });
            const result = await res.json();

            if (!res.ok) {
                const errorCode = result.error;
                const errorMessages = {
                    ALREADY_WAITING_PAYMENT: '이미 결제 대기 중입니다.',
                    ALREADY_JOINED: '이미 참여 완료하였습니다.',
                    ALREADY_IN_WAITING_LIST: '이미 대기열에 등록되어 있습니다.',
                    PRODUCT_NOT_FOUND: '상품이 존재하지 않습니다.'
                };
                alert(errorMessages[errorCode] || '참여 중 오류가 발생하였습니다.');

                if (errorCode === 'ALREADY_WAITING_PAYMENT') {
                    await handlePayment();
                }
                return;
            }

            if (result.status === 'WAITING_LIST') {
                alert('정원이 가득 차서 대기에 등록되었습니다.');
                await reloadParticipationInfo();
            } else {
                await handlePayment();
            }
        } catch (err) {
            console.error('참여 요청 실패:', err);
            alert('네트워크 오류가 발생했습니다.');
        }
    };

    const handlePayment = async () => {
        try {
            setIsLoading(true);
            const orderRes = await fetch(`${API_BASE}/ordr/single?productId=${id}`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                }
            });
            const data = await orderRes.json();

            IMP.init("imp04844503");
            IMP.request_pay({
                pg: 'html5_inicis',
                merchant_uid: data.orderUid,
                name: data.orderItems[0].product.productName,
                amount: data.totalPrice
            }, async (rsp) => {
                if (rsp.success) {
                    await fetch(`${API_BASE}/ordr/payment`, {
                        method: "POST",
                        headers: {
                            'Content-Type': 'application/json',
                            Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                        },
                        body: JSON.stringify({
                            payment_uid: rsp.imp_uid,
                            order_uid: rsp.merchant_uid
                        })
                    });

                    await fetch(`${API_BASE}/participant/confirm?productId=${id}`, {
                        method: "POST",
                        headers: {
                            Authorization: `Bearer ${localStorage.getItem('onion_token')}`
                        }
                    });

                    alert("🎉 결제 완료! 여행에 합류 되셨습니다.");
                    navigate('/');
                } else {
                    alert("결제가 취소되었거나 실패하였습니다.");
                    setIsLoading(false);
                }
            });
        } catch (err) {
            console.error("결제 처리 실패:", err);
            setIsLoading(false);
        }
    };

    const handleCancelJoin = async () => {
        const res = await fetch(`${API_BASE}/ordr/cancel?productId=${id}`, {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${localStorage.getItem('onion_token')}`
            }
        });

        if (res.ok) {
            alert('참여 취소 완료!');
            await reloadParticipationInfo();
        } else {
            alert('참여 취소 실패');
        }
    };

    const handleCancelWaiting = async () => {
        const res = await fetch(`${API_BASE}/participant/cancel/${id}`, {
            method: 'DELETE',
            headers: {
                Authorization: `Bearer ${localStorage.getItem('onion_token')}`
            }
        });

        if (res.ok) {
            alert('대기 취소 완료!');
            await reloadParticipationInfo();
        } else {
            alert('대기 취소 실패');
        }
    };

    const loadStats = async () => {
        toast.info("📊 연령대 통계를 불러오는 중입니다...");
        try {
            const res = await fetch(`${API_BASE}/participant/summary/${id}`);
            const data = await res.json();
            const chartData = Object.entries(data.ageStats).map(([key, value]) => ({
                name: key,
                count: value
            }));
            setAgeStats(chartData);
            setJoinedCount(data.joinedCount);
            setShowStats(true);
        } catch (err) {
            console.error('통계 불러오기 실패:', err);
            alert('참여자가 없을 경우 통계 데이터를 불러오지 못했어요!');
        }
    };

    const renderActionButton = () => {
        if (participationStatus === 'JOINED') {
            return <button className="btn btn-danger mt-3" onClick={handleCancelJoin}>참여 취소하기</button>;
        } else if (participationStatus === 'WAITING_LIST') {
            return <button className="btn btn-warning mt-3" onClick={handleCancelWaiting}>대기 취소하기</button>;
        } else if (participationStatus === 'WAITING_PAYMENT') {
            return <button className="btn btn-success mt-3" onClick={handlePayment}>💳 결제 진행하기</button>;
        } else {
            return (
                <button className="btn btn-primary mt-3" onClick={handleJoin}>
                    {joinedCount >= product.capacity ? '대기 신청하기' : '참여하기'}
                </button>
            );
        }
    };

    if (!product) return <div className="text-center">불러오는 중...</div>;

    return (
        <div className="container my-5">
            {isLoading && (
                <div className="position-absolute top-0 start-0 w-100 h-100 bg-dark bg-opacity-75 d-flex flex-column justify-content-center align-items-center" style={{ zIndex: 1050 }}>
                    <div className="spinner-border text-light mb-3" role="status" />
                    <p className="text-white fs-5">💳 결제 준비 중입니다...</p>
                </div>
            )}
            <div className="row">
                <div className="col-md-6">
                    <img
                        src={product.imageUrl}
                        alt={product.productName}
                        className="img-fluid rounded shadow-sm"
                        style={{ maxHeight: '400px', objectFit: 'cover', width: '100%' }}
                    />
                </div>
                <div className="col-md-6 d-flex flex-column justify-content-center">
                    <h2 className="mb-3">{product.productName}</h2>
                    <p className="text-muted" style={{ whiteSpace: 'pre-line' }}>{product.description}</p>
                    <p className="fw-bold">모집 인원: {joinedCount} / {product.capacity}</p>

                    {renderActionButton()}

                    <button className="btn btn-outline-secondary mt-3" onClick={loadStats}>
                        연령대 통계 보기
                    </button>

                    <button className="btn btn-outline-info mt-3" onClick={() => setShowChat(true)}>
                        실시간 채팅
                    </button>

                    {participationStatus === 'WAITING_PAYMENT' && timeLeft > 0 && (
                        <p className="text-danger mt-3">
                            ⏳ 결제 마감 시간: {deadlineText}
                        </p>
                    )}
                </div>
            </div>

            <ChatRoom
                productId={id}
                title={product.productName}
                username={nickName}
                show={showChat}
                onHide={() => setShowChat(false)}
            />

            <StatisticsModal
                show={showStats}
                onHide={() => setShowStats(false)}
                data={ageStats}
            />
        </div>
    );
}

export default ProductDetail;
