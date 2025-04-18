import React, { useEffect, useRef, useState } from 'react';
import { Modal, Button } from 'react-bootstrap';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

function ChatRoom({ productId, title, username, show, onHide }) {
    const [stompClient, setStompClient] = useState(null);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');

    const pageRef = useRef(0);
    const hasMoreRef = useRef(true);
    const loadingRef = useRef(false);
    const isFirstLoadDone = useRef(false);

    const messageBoxRef = useRef(null);
    const messagesEndRef = useRef(null);

    const loginYn = !!username;
    const roomId = productId;

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    const WS_BASE =
        process.env.NODE_ENV === "development"
            ? "http://localhost:8080/ws"
            : "/ws";

    const fetchMessages = async (targetPage) => {
        loadingRef.current = true;

        try {
            const res = await fetch(`${API_BASE}/chat/${roomId}?page=${targetPage}&size=50`);
            if (!res.ok) throw new Error('응답 실패');

            const newMessages = await res.json();

            if (newMessages.length < 50) hasMoreRef.current = false;

            const box = messageBoxRef.current;
            const prevScrollHeight = box.scrollHeight;
            const prevScrollTop = box.scrollTop;

            setMessages(prev => [...newMessages.reverse(), ...prev]);

            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    const newScrollHeight = box.scrollHeight;
                    if (targetPage === 0) {
                        box.scrollTop = newScrollHeight;
                        isFirstLoadDone.current = true;
                    } else {
                        const addedHeight = newScrollHeight - prevScrollHeight;
                        box.scrollTop = prevScrollTop + addedHeight;
                    }
                });
            });
        } catch (err) {
            console.error("메시지 불러오기 에러:", err);
        } finally {
            loadingRef.current = false;
        }
    };

    const handleScroll = () => {
        const box = messageBoxRef.current;
        if (!box || !isFirstLoadDone.current || loadingRef.current) return;

        if (box.scrollTop <= 10 && hasMoreRef.current) {
            const nextPage = pageRef.current + 1;
            pageRef.current = nextPage;
            fetchMessages(nextPage);
        }
    };

    useEffect(() => {
        if (show) {
            setMessages([]);
            pageRef.current = 0;
            hasMoreRef.current = true;
            loadingRef.current = false;
            isFirstLoadDone.current = false;

            fetchMessages(0);
        }
    }, [roomId, show]);

    useEffect(() => {
        // const socket = new SockJS(WS_BASE);
        // const client = Stomp.over(socket);
        const socket = new SockJS(WS_BASE); // 예: http://localhost:8080/ws
        console.log(socket);
        const client = Stomp.over(() => socket);
        client.debug = () => {};

        client.connect({}, () => {
            client.subscribe(`/topic/chat/${roomId}`, (msg) => {
                const newMessage = JSON.parse(msg.body);
                setMessages(prev => [...prev, newMessage]);
            });
            setStompClient(client);
        });

        return () => {
            if (client) client.disconnect();
        };
    }, [roomId, username]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const sendMessage = () => {
        if (!loginYn) {
            alert("로그인이 필요합니다");
            return;
        }
        if (stompClient && input.trim() !== '') {
            stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
                type: 'TALK',
                roomId,
                sender: username,
                message: input
            }));
            setInput('');
        }
    };

    return (
        <Modal show={show} onHide={onHide} centered>
            <Modal.Header closeButton>
                <Modal.Title>채팅방 - {title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <div
                    ref={messageBoxRef}
                    onScroll={handleScroll}
                    className="chat-box"
                    style={{
                        maxHeight: '500px',
                        minHeight: '300px',
                        overflowY: 'auto',
                        backgroundColor: '#f8f9fa',
                        borderRadius: '8px',
                        padding: '10px'
                    }}
                >
                    {messages.length === 0 ? (
                        <div className="text-muted text-center mt-5">아직 채팅이 없어요 💤</div>
                    ) : (
                        messages.map((msg, idx) => {
                            if (!msg.message || msg.type === 'ENTER' || !msg.sender) return null;
                            const isMine = msg.sender === username;
                            return (
                                <div key={idx} className={`d-flex mb-2 ${isMine ? 'justify-content-end' : 'justify-content-start'}`}>
                                    <div className={`p-2 rounded-3 ${isMine ? 'bg-primary text-white' : 'bg-light text-dark'}`} style={{ maxWidth: '70%' }}>
                                        <strong className="d-block">{msg.sender}</strong>
                                        <span>{msg.message}</span>
                                    </div>
                                </div>
                            );
                        })
                    )}
                    <div ref={messagesEndRef} />
                </div>

                <div className="mt-3 d-flex">
                    <input
                        type="text"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && sendMessage()}
                        className="form-control me-2"
                        disabled={!loginYn}
                        placeholder={loginYn ? "메시지를 입력하세요" : "로그인 후 채팅 가능"}
                    />
                    <Button onClick={sendMessage}>전송</Button>
                </div>
            </Modal.Body>
        </Modal>
    );
}

export default ChatRoom;
