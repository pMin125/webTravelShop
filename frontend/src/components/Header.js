import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

function Header() {
    const navigate = useNavigate();
    const isLoggedIn = !!localStorage.getItem('onion_token');
    const nickname = localStorage.getItem('nickname');

    const handleLogout = () => {
        localStorage.removeItem('onion_token');
        localStorage.removeItem('nickname');
        alert('로그아웃 되었습니다!');
        window.location.reload();
    };
    return (
        <header className="bg-light p-3 mb-4 border-bottom shadow-sm">
            <div className="container d-flex justify-content-between align-items-center">
                {/* 로고 또는 제목 */}
                <h3 className="m-0">🛍️ MyShop</h3>

                {/* 네비게이션 */}
                <nav>
                    <Link to="/" className="btn btn-outline-primary me-2">홈</Link>
                    <Link to="/signup" className="btn btn-outline-primary me-2">회원가입</Link>
                    <Link to="/cart" className="btn btn-outline-success me-2">장바구니</Link>
                    <Link to="/orders" className="btn btn-outline-secondary me-2">주문 조회</Link>
                    {isLoggedIn ? (
                        <>
                            <button onClick={handleLogout} className="btn btn-outline-danger">로그아웃</button>
                            <span className="me-2 fw-bold">{nickname}님</span>
                        </>
                    ) : (
                        <Link to="/login" className="btn btn-outline-secondary">로그인</Link>
                    )}
                </nav>
            </div>
        </header>
    );
}

export default Header;