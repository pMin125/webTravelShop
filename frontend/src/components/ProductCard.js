import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ProgressBar } from 'react-bootstrap';
import { FaHeart, FaRegHeart } from 'react-icons/fa';
import { useUser } from "../context/UserContext";

function ProductCard({ product, liked }) {
    const [isLiked, setIsLiked] = useState(liked);
    const { isLogin, loginChecked } = useUser();

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    useEffect(() => {
        setIsLiked(liked);
    }, [liked]);

    const handleLikeClick = async (e) => {
        e.preventDefault();
        e.stopPropagation();

        if (!loginChecked || !isLogin) {
            alert("로그인을 해주세요!");
            return;
        }

        const newLiked = !isLiked;
        setIsLiked(newLiked);

        try {
            const url = newLiked
                ? `${API_BASE}/cart/add?productId=${product.id}&quantity=1`
                : `${API_BASE}/cart/remove?productId=${product.id}`;

            const res = await fetch(url, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('onion_token')}`,
                },
            });

            if (!res.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }

            alert(
                newLiked
                    ? '장바구니에 추가되었습니다!'
                    : '장바구니에서 제거되었습니다!'
            );
        } catch (err) {
            console.error('장바구니 처리 실패:', err);
            alert('상품을 장바구니에서 처리하는 데 실패했습니다!');
        }
    };

    return (
        <div className="col-md-4 mb-4">
            <div className="card h-100 shadow-sm position-relative">
                <Link to={`/product/${product.id}`} className="text-decoration-none text-dark">
                    <img
                        src={product.imageUrl}
                        className="card-img-top"
                        alt={product.productName}
                    />
                    <div className="card-body d-flex flex-column justify-content-between">
                        <div>
                            <h5 className="card-title mb-2 d-flex justify-content-between align-items-center">
                                {product.productName}
                                <button
                                    onClick={handleLikeClick}
                                    style={{
                                        background: 'none',
                                        border: 'none',
                                        cursor: 'pointer',
                                    }}
                                >
                                    {isLiked ? <FaHeart color="red" size={20} /> : <FaRegHeart size={20} />}
                                </button>
                            </h5>
                            <div className="mb-2">
                                <small className="text-muted">참여자 수</small>
                                <ProgressBar
                                    now={(product.joinedCount / product.capacity) * 100}
                                    label={`${product.joinedCount}/${product.capacity}`}
                                    variant={product.joinedCount === product.capacity ? "success" : "info"}
                                />
                            </div>
                            <p className="text-muted mb-2">
                                🗓️ {product.createdDate?.slice(0, 10).replace(/-/g, '.')} ~{' '}
                                {product.endDate?.slice(0, 10).replace(/-/g, '.')}
                            </p>
                            <p className="card-text description">
                                {product.description}
                            </p>
                        </div>
                        <div className="mt-3">
                            <span className="btn btn-outline-primary w-100">참여하기</span>
                        </div>
                    </div>
                </Link>
            </div>
        </div>
    );
}

export default ProductCard;
