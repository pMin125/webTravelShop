import React, { useState } from 'react';

const SignUp = () => {
    const [email, setEmail] = useState('');
    const [username, setUsername] = useState('');
    const [nickName, setNickName] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }

        const signUpData = { email, username, nickName, password };

        try {
            const res = await fetch(`${API_BASE}/signUp`, {
                method: "POST",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(signUpData),
            });

            if (!res.ok) {
                throw new Error('회원가입에 실패했습니다.');
            }

            await res.json();
            setSuccess(true);
            setError('');
        } catch (err) {
            setError('회원가입에 실패했습니다. 다시 시도해주세요.');
            console.error(err);
        }
    };

    return (
        <div className="signup-form container mt-5">
            <h2 className="mb-4">회원가입</h2>
            {success && <p className="text-success">회원가입이 성공적으로 완료되었습니다!</p>}
            {error && <p className="text-danger">{error}</p>}
            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label className="form-label">Email</label>
                    <input
                        type="email"
                        className="form-control"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Username</label>
                    <input
                        type="text"
                        className="form-control"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Nick Name</label>
                    <input
                        type="text"
                        className="form-control"
                        value={nickName}
                        onChange={(e) => setNickName(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Password</label>
                    <input
                        type="password"
                        className="form-control"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <label className="form-label">Confirm Password</label>
                    <input
                        type="password"
                        className="form-control"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">회원가입</button>
            </form>
        </div>
    );
};

export default SignUp;
