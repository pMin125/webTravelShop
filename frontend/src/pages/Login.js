import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    const handleLogin = async (e) => {
        e.preventDefault();

        try {
            const response = await fetch(`${API_BASE}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('onion_token', data.token);
                localStorage.setItem('nickname', data.nickname);
                localStorage.setItem('username', data.name);
                alert('로그인 성공!');
                navigate('/');
            } else {
                alert('로그인 실패: 아이디 또는 비밀번호를 확인해주세요.');
            }
        } catch (err) {
            console.error('로그인 요청 중 오류:', err);
            alert('서버 오류가 발생했습니다.');
        }
    };

    return (
        <div className="container mt-5">
            <h2>로그인</h2>
            <form onSubmit={handleLogin}>
                <div className="mb-3">
                    <label htmlFor="username" className="form-label">아이디</label>
                    <input
                        type="text"
                        className="form-control"
                        id="username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label htmlFor="password" className="form-label">비밀번호</label>
                    <input
                        type="password"
                        className="form-control"
                        id="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button type="submit" className="btn btn-primary">로그인</button>
            </form>
        </div>
    );
}

export default Login;
