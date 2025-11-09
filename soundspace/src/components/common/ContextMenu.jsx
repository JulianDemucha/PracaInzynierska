import React, { useState, useEffect, useRef } from 'react';
import './ContextMenu.css';
import moreIcon from '../../assets/images/3dots.png';
import moreIconOn from '../../assets/images/3dotsOn.png';
/**
 * Komponent generycznego menu kontekstowego.
 * @param {Object[]} options - Tablica obiektów opcji, np. [{ label: 'Nazwa opcji', onClick: () => {} }]
 */
function ContextMenu({ options }) {
    const [isVisible, setIsVisible] = useState(false);
    const [position, setPosition] = useState({});
    const menuRef = useRef(null);
    const triggerRef = useRef(null);
    const toggleMenu = (e) => {
        e.stopPropagation();
        if (!isVisible) {
            const rect = triggerRef.current.getBoundingClientRect();
            const viewportHeight = window.innerHeight;
            const viewportWidth = window.innerWidth;

            let newPosition = {};

            if (rect.bottom > viewportHeight / 2) {
                newPosition.bottom = '110%';
            } else {
                newPosition.top = '110%';
            }
            if (rect.left < (viewportWidth / 2)) {
                newPosition.left = 0;
            } else {
                newPosition.right = 0;
            }
            setPosition(newPosition);
        }
        setIsVisible(!isVisible);
    };
    const handleOptionClick = (optionOnClick) => {
        optionOnClick();
        setIsVisible(false);
    };
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setIsVisible(false);
            }
        };
        if (isVisible) {
            document.addEventListener('mousedown', handleClickOutside);
        }
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isVisible]);

    return (
        <div className="context-menu-container" ref={menuRef}>
            <button
                className="context-menu-trigger"
                onClick={toggleMenu}
                ref={triggerRef}
            >
                {isVisible ? (
                    <img src={moreIconOn} alt="Więcej (Otwarte)" />
                ) : (
                    <img src={moreIcon} alt="Więcej (Zamknięte)" />
                )}
            </button>

            {isVisible && (
                <ul
                    className="context-menu-popup"
                    style={position}
                >
                    {options.map((option, index) => (
                        <li
                            key={index}
                            className="context-menu-option"
                            onClick={() => handleOptionClick(option.onClick)}
                        >
                            {option.label}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
export default ContextMenu;