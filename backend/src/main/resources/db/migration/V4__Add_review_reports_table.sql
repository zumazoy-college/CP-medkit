CREATE TABLE review_reports (
    id_report SERIAL PRIMARY KEY,
    review_id INTEGER NOT NULL,
    reporter_id INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_reports_review FOREIGN KEY (review_id) REFERENCES reviews(id_review) ON DELETE CASCADE,
    CONSTRAINT fk_review_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id_user) ON DELETE CASCADE
);

CREATE INDEX idx_review_reports_review_id ON review_reports(review_id);
CREATE INDEX idx_review_reports_reporter_id ON review_reports(reporter_id);
CREATE INDEX idx_review_reports_status ON review_reports(status);
