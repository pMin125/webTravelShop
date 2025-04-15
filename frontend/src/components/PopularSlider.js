import React from "react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import "../pages/css/Home.css";

const PrevArrow = ({ className, style, onClick }) => (
    <div
        className={className}
        onClick={onClick}
        style={{
            ...style,
            display: "block",
            left: "-70px",
            zIndex: 10,
            fontSize: "2rem",
            color: "#333",
        }}
    >
        ◀
    </div>
);

const NextArrow = ({ className, style, onClick }) => (
    <div
        className={className}
        onClick={onClick}
        style={{
            ...style,
            display: "block",
            right: "-30px",
            zIndex: 10,
            fontSize: "2rem",
            color: "#333",
        }}
    >
        ▶
    </div>
);

const PopularSlider = ({ popularList }) => {
    const settings = {
        slidesToShow: 4,
        slidesToScroll: 4,
        arrows: true,
        infinite: false,
        speed: 500,
        prevArrow: <PrevArrow />,
        nextArrow: <NextArrow />,
    };

    return (
        <Slider {...settings}>
            {popularList.map((product) => (
                <div key={product.id} className="px-2">
                    <div className="card popular-card h-100">
                        <img
                            src={product.imageUrl}
                            className="card-img-top"
                            alt={product.title}
                        />
                        <div className="card-body">
                            <h5 className="card-title">{product.title}</h5>
                            <p>참여 + 대기: {product.participantCount} 명</p>
                        </div>
                    </div>
                </div>
            ))}
        </Slider>
    );
};

export default PopularSlider;
