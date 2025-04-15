import React, { useEffect, useState } from 'react';
import './css/Home.css';
import { Link } from 'react-router-dom';
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import PopularSlider from "../components/PopularSlider";
import ProductCard from '../components/ProductCard';
import { useUser } from '../context/UserContext';

function HomePage() {
    const [productList, setProductList] = useState([]);
    const [popularList, setPopularList] = useState([]);
    const [selectedTag, setSelectedTag] = useState('전체');
    const [heartedProductIds, setHeartedProductIds] = useState([]);
    const tags = ['전체', '자연', '도심', '맛집', '액티비티', '휴양'];
    const { isLogin, loginChecked } = useUser();

    const API_BASE =
        process.env.NODE_ENV === "development"
            ? process.env.REACT_APP_API_BASE || "http://localhost:8080"
            : "/api";

    const filteredProducts =
        selectedTag === '전체'
            ? productList
            : productList.filter(product => product.tagNames.includes(selectedTag));

    useEffect(() => {
        // 전체 상품
        fetch(`${API_BASE}/product/productList`)
            .then(res => res.json())
            .then(setProductList)
            .catch(err => console.error('상품 목록 불러오기 실패:', err));

        // 인기 상품
        fetch(`${API_BASE}/product/popular`)
            .then(res => res.json())
            .then(setPopularList)
            .catch(err => console.error('인기 상품 불러오기 실패:', err));
    }, [API_BASE]);

    useEffect(() => {
        if (!loginChecked || !isLogin) return;

        fetch(`${API_BASE}/heart/me`, {
            headers: {
                Authorization: `Bearer ${localStorage.getItem('onion_token')}`,
            },
        })
            .then(res => res.json())
            .then(setHeartedProductIds)
            .catch(err => console.error("하트 API 실패:", err));
    }, [loginChecked, isLogin, API_BASE]);

    if (!loginChecked) {
        return <div className="text-center py-5">⏳ 로그인 확인 중...</div>;
    }

    return (
        <>
            <h2 className="section-title">🔥 인기 여행</h2>
            <div className="mb-5">
                <PopularSlider popularList={popularList} />
            </div>
            <h2 className="section-title">🔥 태그별 여행</h2>
            <div className="mb-4 d-flex flex-wrap">
                {tags.map(tag => (
                    <button
                        key={tag}
                        type="button"
                        onClick={() => setSelectedTag(tag)}
                        className={`tag-button ${selectedTag === tag ? 'active' : ''}`}
                    >
                        #{tag}
                    </button>
                ))}
            </div>
            <div className="container">
                <div className="row">
                    {filteredProducts.length === 0 ? (
                        <p className="text-muted">해당 태그의 여행이 없습니다.</p>
                    ) : (
                        filteredProducts.map(product => (
                            <ProductCard
                                key={product.id}
                                product={product}
                                liked={heartedProductIds.includes(product.id)}
                            />
                        ))
                    )}
                </div>
            </div>
        </>
    );
}

export default HomePage;
