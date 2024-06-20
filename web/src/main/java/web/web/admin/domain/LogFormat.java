package web.web.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class LogFormat {
    @Id @GeneratedValue
    @Column(name = "log_format_id")
    private Long id;

    private int idsite;
    private int e_a;
    private int e_c;
    private int REMOTE_ADDR;
    private int rec;
    private int java;
    private int e_n;
    private int pf_dm1;
    private int uadata;
    private int e_v;
    private int pf_tfr;
    private int memberId;
    private int HTTP_ORIGIN;
    private int send_image;
    private int res;
    private int qt;
    private int cookie;
    private int HTTP_ACCEPT_LANGUAGE;
    private int ag;
    private int HTTP_X_REAL_IP;
    private int HTTP_CONTENT_TYPE;
    private int HTTP_X_FORWARDED_PROTO;
    private int _event_record;
    private int HTTP_USER_AGENT;
    private int _id;
    private int pf_net;
    private int _refts;
    private int pf_srv;
    private int wma;
    private int pf_onl;
    private int HTTP_REFERER;
    private int _idn;
    private int fla;
    private int ca;
    private int urlref;
    private int HTTP_CONTENT_LENGTH;
    private int realp;
    private int h;
    private int pf_dm2;
    private int m;
    private int url;
    private int r;
    private int HTTP_HOST;
    private int s;
    private int pdf;
    private int HTTP_ACCEPT;
    private int HTTP_X_FORWARDED_FOR;
    private int HTTP_ACCEPT_ENCODING;
    private int HTTP_CONNECTION;
    private int date;

    // 기본 생성자
    public LogFormat() {
        this.idsite = 0;
        this.e_a = 0;
        this.e_c = 0;
        this.REMOTE_ADDR = 0;
        this.rec = 0;
        this.java = 0;
        this.e_n = 0;
        this.pf_dm1 = 0;
        this.uadata = 0;
        this.e_v = 0;
        this.pf_tfr = 0;
        this.memberId = 0;
        this.HTTP_ORIGIN = 0;
        this.send_image = 0;
        this.res = 0;
        this.qt = 0;
        this.cookie = 0;
        this.HTTP_ACCEPT_LANGUAGE = 0;
        this.ag = 0;
        this.HTTP_X_REAL_IP = 0;
        this.HTTP_CONTENT_TYPE = 0;
        this.HTTP_X_FORWARDED_PROTO = 0;
        this._event_record = 0;
        this.HTTP_USER_AGENT = 0;
        this._id = 0;
        this.pf_net = 0;
        this._refts = 0;
        this.pf_srv = 0;
        this.wma = 0;
        this.pf_onl = 0;
        this.HTTP_REFERER = 0;
        this._idn = 0;
        this.fla = 0;
        this.ca = 0;
        this.urlref = 0;
        this.HTTP_CONTENT_LENGTH = 0;
        this.realp = 0;
        this.h = 0;
        this.pf_dm2 = 0;
        this.m = 0;
        this.url = 0;
        this.r = 0;
        this.HTTP_HOST = 0;
        this.s = 0;
        this.pdf = 0;
        this.HTTP_ACCEPT = 0;
        this.HTTP_X_FORWARDED_FOR = 0;
        this.HTTP_ACCEPT_ENCODING = 0;
        this.HTTP_CONNECTION = 0;
        this.date = 1;
    }

}
